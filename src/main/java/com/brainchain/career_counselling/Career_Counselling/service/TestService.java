package com.brainchain.career_counselling.Career_Counselling.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.brainchain.career_counselling.Career_Counselling.dto.AnswerRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.QuestionDTO;
import com.brainchain.career_counselling.Career_Counselling.dto.TestResponse;
import com.brainchain.career_counselling.Career_Counselling.entity.CareerTest;
import com.brainchain.career_counselling.Career_Counselling.entity.Question;
import com.brainchain.career_counselling.Career_Counselling.entity.TestResult;
import com.brainchain.career_counselling.Career_Counselling.entity.User;
import com.brainchain.career_counselling.Career_Counselling.entity.UserResponse;
import com.brainchain.career_counselling.Career_Counselling.exceptions.QuestionNotFoundException;
import com.brainchain.career_counselling.Career_Counselling.repository.CareerTestRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.QuestionRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.TestResultRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.UserRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.UserResponseRepository;

import jakarta.transaction.Transactional;

@Service
public class TestService {

    private final UserRepository userRepository;
    private final CareerTestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final TestResultRepository testResultRepository;
    private final UserResponseRepository userResponseRepository;

    public TestService(UserRepository userRepository,
                       CareerTestRepository testRepository,
                       QuestionRepository questionRepository,
                       TestResultRepository testResultRepository,
                       UserResponseRepository userResponseRepository) {
        this.userRepository = userRepository;
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.testResultRepository = testResultRepository;
        this.userResponseRepository = userResponseRepository;
    }

    /**
     * Starts the test for a user, initializes a TestResult if not already present.
     */
    @Transactional
    public TestResponse startTest(String userEmail, Integer testId) {
        validateInputs(userEmail, testId);

        User user = getUserByEmail(userEmail);
        CareerTest test = getTestById(testId);

        if (!"IQ Test".equalsIgnoreCase(test.getTestName())) {
            test.setValidateAnswer(false); // Disable validation for surveys
        }

        List<QuestionDTO> questions = getQuestionsForTest(testId);
        TestResult testResult = testResultRepository.findByUserIdAndTest_Id(user.getId(), testId)
                .orElseGet(() -> saveNewTestResult(user, test));

        QuestionDTO nextQuestion = getNextQuestion(testResult, questions);

        if (nextQuestion == null) {
            return TestResponse.builder()
                    .testId(testId)
                    .message("Test completed. Please submit to get your results.")
                    .build();
        }

        testResult.setLastQuestionId(nextQuestion.getQuestionId());
        testResultRepository.save(testResult);

        return TestResponse.builder()
                .testId(testId)
                .nextQuestion(nextQuestion)
                .message("Next question loaded successfully.")
                .build();
    }

    /**
     * Returns a specific question for a given test.
     */
    public QuestionDTO getQuestion(Integer testId, Long questionId) {
        if (testId == null || questionId == null) {
            throw new IllegalArgumentException("Test ID and Question ID cannot be null.");
        }

        Question question = questionRepository.findByQuestionIdAndTest_Id(questionId, testId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found for the given test and ID."));

        return mapToQuestionDTO(question);
    }

    /**
     * Submits an answer to a given question and moves to the next.
     */
    @Transactional
    public TestResponse submitAnswer(AnswerRequest request) {
        validateAnswerRequest(request);

        User user = getUserById(request.getUserId());
        CareerTest test = getTestById(request.getTestId());
        Question question = getQuestionByIdAndTest(request.getQuestionId(), request.getTestId());

        TestResult testResult = testResultRepository.findByUserIdAndTest_Id(user.getId(), test.getId())
                .orElseGet(() -> saveNewTestResult(user, test));

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setTestResult(testResult);
        response.setQuestion(question);
        response.setUserAnswer(request.getUserAnswer());

        if ("IQ Test".equalsIgnoreCase(test.getTestName())) {
            response.setIsCorrect(request.getUserAnswer().equals(question.getCorrectAnswer()));
        }

        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        userResponseRepository.save(response);

        List<QuestionDTO> questions = getQuestionsForTest(test.getId());
        QuestionDTO nextQuestion = getNextQuestionFromList(request.getQuestionId(), questions);

        testResult.setLastQuestionId(nextQuestion != null ? nextQuestion.getQuestionId() : null);
        testResultRepository.save(testResult);

        String message = (nextQuestion == null)
                ? "Answer submitted. Test completed. Please wait for your results."
                : "Answer submitted. Next question loaded successfully.";

        return TestResponse.builder()
                .testId(test.getId())
                .nextQuestion(nextQuestion)
                .message(message)
                .build();
    }

    /**
     * Retakes a test: clears responses and resets the result.
     */
    @Transactional
    public TestResponse retakeTest(String userEmail, Integer testId) {
        User user = getUserByEmail(userEmail);
        CareerTest test = getTestById(testId);

        userResponseRepository.deleteAll(
                userResponseRepository.findByUserIdAndTestResultTestId(user.getId(), testId)
        );

        TestResult result = testResultRepository.findByUserIdAndTest_Id(user.getId(), testId)
                .orElseGet(() -> saveNewTestResult(user, test));

        result.setLastQuestionId(null);
        result.setScore(null);
        result.setStatus(TestResult.Status.IN_PROGRESS);
        result.setCompletedAt(null);
        result.setPausedAt(null);
        testResultRepository.save(result);

        List<QuestionDTO> questions = getQuestionsForTest(testId);

        result.setLastQuestionId(questions.get(0).getQuestionId());
        testResultRepository.save(result);

        return TestResponse.builder()
                .testId(testId)
                .nextQuestion(questions.get(0))
                .message("Retake started. Your previous responses have been cleared.")
                .build();
    }

    /**
     * Generates the final score and marks the test result as complete.
     */
    @Transactional
    public TestResult generateTestResult(String userEmail, Integer testId) {
        validateInputs(userEmail, testId);

        User user = getUserByEmail(userEmail);
        CareerTest test = getTestById(testId);

        List<QuestionDTO> questions = getQuestionsForTest(testId);
        List<UserResponse> responses = userResponseRepository.findByUserIdAndTestResultTestId(user.getId(), testId);

        int score = 0;
        if (!responses.isEmpty() && "IQ Test".equalsIgnoreCase(test.getTestName())) {
            long correctCount = responses.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsCorrect()))
                    .count();
            score = (int) ((correctCount * 100.0) / questions.size());
        }

        TestResult result = testResultRepository.findByUserIdAndTest_Id(user.getId(), testId)
                .orElseGet(() -> saveNewTestResult(user, test));

        result.setScore(score);
        result.setStatus(TestResult.Status.COMPLETED);
        result.setCompletedAt(LocalDateTime.now());
        return testResultRepository.save(result);
    }

    /**
     * Pauses the current test for the user.
     */
    @Transactional
    public TestResult pauseTest(String userEmail, Integer testId) {
        validateInputs(userEmail, testId);

        User user = getUserByEmail(userEmail);
        TestResult result = testResultRepository.findByUserIdAndTest_Id(user.getId(), testId)
                .orElseThrow(() -> new IllegalArgumentException("Test result not found."));

        result.setStatus(TestResult.Status.PAUSED);
        result.setPausedAt(LocalDateTime.now());
        return testResultRepository.save(result);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 🔧 Helper Methods
    // ─────────────────────────────────────────────────────────────────────────────

    private QuestionDTO getNextQuestion(TestResult testResult, List<QuestionDTO> questions) {
        if (testResult.getLastQuestionId() == null) {
            return questions.get(0);
        }

        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getQuestionId().equals(testResult.getLastQuestionId()) && i + 1 < questions.size()) {
                return questions.get(i + 1);
            }
        }
        return null;
    }

    private QuestionDTO getNextQuestionFromList(Long currentQId, List<QuestionDTO> questions) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getQuestionId().equals(currentQId) && i + 1 < questions.size()) {
                return questions.get(i + 1);
            }
        }
        return null;
    }

    private TestResult saveNewTestResult(User user, CareerTest test) {
        TestResult result = new TestResult();
        result.setUser(user);
        result.setTest(test);
        result.setStatus(TestResult.Status.IN_PROGRESS);
        return testResultRepository.save(result);
    }

    private void validateInputs(String email, Integer testId) {
        if (email == null || testId == null) {
            throw new IllegalArgumentException("Email and Test ID must not be null.");
        }
    }

    private void validateAnswerRequest(AnswerRequest request) {
        if (request == null || request.getUserId() == null || request.getTestId() == null ||
                request.getQuestionId() == null || request.getUserAnswer() == null) {
            throw new IllegalArgumentException("Invalid answer submission request.");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    private CareerTest getTestById(Integer testId) {
        return testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + testId));
    }

    private Question getQuestionByIdAndTest(Long questionId, Integer testId) {
        return questionRepository.findByQuestionIdAndTest_Id(questionId, testId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found."));
    }

    private List<QuestionDTO> getQuestionsForTest(Integer testId) {
        List<Question> questions = questionRepository.findByTest_Id(testId);
        if (questions.isEmpty()) {
            throw new QuestionNotFoundException("No questions found for the test.");
        }
        return questions.stream().map(this::mapToQuestionDTO).collect(Collectors.toList());
    }

    private QuestionDTO mapToQuestionDTO(Question question) {
        return QuestionDTO.builder()
                .testId(question.getTest().getId())
                .questionId(question.getQuestionId())
                .question(question.getQuestion())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .correctAnswer(question.getCorrectAnswer())
                .build();
    }
}
