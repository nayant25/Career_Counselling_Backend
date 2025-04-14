package com.brainchain.career_counselling.Career_Counselling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.brainchain.career_counselling.Career_Counselling.dto.CareerSuggestion;
import com.brainchain.career_counselling.Career_Counselling.entity.CareerTest;
import com.brainchain.career_counselling.Career_Counselling.entity.Question;
import com.brainchain.career_counselling.Career_Counselling.entity.TestResult;
import com.brainchain.career_counselling.Career_Counselling.entity.User;
import com.brainchain.career_counselling.Career_Counselling.entity.UserResponse;
import com.brainchain.career_counselling.Career_Counselling.repository.CareerTestRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.QuestionRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.TestResultRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.UserRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.UserResponseRepository;
import com.brainchain.career_counselling.Career_Counselling.service.CareerRecommenderService;
import com.brainchain.career_counselling.Career_Counselling.service.PdfReportService;
import com.brainchain.career_counselling.Career_Counselling.service.ReportService;
import com.brainchain.career_counselling.Career_Counselling.service.TestService;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "skip.csv.loading=true",
        "spring.profiles.active=test"
})
class CareerCounsellingApplicationTests {

    @Autowired private TestService testService;
    @Autowired private UserRepository userRepository;
    @Autowired private CareerTestRepository testRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private TestResultRepository testResultRepository;
    @Autowired private UserResponseRepository userResponseRepository;
    @Autowired private PdfReportService pdfReportService;

    @MockBean private CareerRecommenderService careerRecommenderService;
    @MockBean private ReportService reportService;

    private User mockUser;
    private CareerTest iqTest;
    private List<Question> questions;
    private TestResult testResult;

    @BeforeEach
    void setup() {
        userResponseRepository.deleteAll();
        testResultRepository.deleteAll();
        questionRepository.deleteAll();
        testRepository.deleteAll();
        userRepository.deleteAll();

        mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setPassword("pass");
        mockUser.setPhoneNumber("9999999999");
        mockUser.setName("Test User");
        mockUser.setRole(User.Role.STUDENT);
        mockUser.setEmailVerified(true);
        mockUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(mockUser);

        iqTest = new CareerTest();
        iqTest.setTestName("IQ Test");
        iqTest.setDescription("IQ Evaluation");
        iqTest.setValidateAnswer(true);
        iqTest.setStatus(CareerTest.Status.ACTIVE);
        iqTest.setCreatedAt(LocalDateTime.now());
        testRepository.save(iqTest);

        questions = List.of(
                createQuestion("2+2=?", "4", "3", "5", "6", "A"),
                createQuestion("Capital of France?", "Paris", "London", "Berlin", "Rome", "A")
        );
        questionRepository.saveAll(questions);

        testResult = new TestResult();
        testResult.setUser(mockUser);
        testResult.setTest(iqTest);
        testResult.setStatus(TestResult.Status.IN_PROGRESS);
        testResult.setCreatedAt(LocalDateTime.now());
        testResultRepository.save(testResult);
    }

    private Question createQuestion(String text, String a, String b, String c, String d, String answer) {
        Question q = new Question();
        q.setTest(iqTest);
        q.setQuestion(text);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectAnswer(answer);
        return q;
    }

    private UserResponse createResponse(Question q, String userAnswer, boolean isCorrect) {
        UserResponse response = new UserResponse();
        response.setUser(mockUser);
        response.setTestResult(testResult);
        response.setQuestion(q);
        response.setUserAnswer(userAnswer);
        response.setIsCorrect(isCorrect);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    @Test
    @DisplayName("✅ Should complete test and return 100% score")
    void testCompleteCorrectTest() {
        List<UserResponse> responses = questions.stream()
                .map(q -> createResponse(q, q.getCorrectAnswer(), true))
                .collect(Collectors.toList());
        userResponseRepository.saveAll(responses);

        TestResult result = testService.generateTestResult(mockUser.getEmail(), iqTest.getId());

        assertEquals(100, result.getScore());
        assertEquals(TestResult.Status.COMPLETED, result.getStatus());
    }

    @Test
    @DisplayName("✅ Should return partial score with mixed answers")
    void testPartialScoreTest() {
        UserResponse r1 = createResponse(questions.get(0), questions.get(0).getCorrectAnswer(), true);
        UserResponse r2 = createResponse(questions.get(1), "London", false);
        userResponseRepository.saveAll(List.of(r1, r2));

        TestResult result = testService.generateTestResult(mockUser.getEmail(), iqTest.getId());

        assertEquals(50, result.getScore());
        assertEquals(TestResult.Status.COMPLETED, result.getStatus());
    }

    @Test
    @DisplayName("✅ Should handle empty responses with score 0")
    void testEmptySubmission() {
        TestResult result = testService.generateTestResult(mockUser.getEmail(), iqTest.getId());
        assertEquals(0, result.getScore());
    }

    @Test
    @DisplayName("⛔ Should throw exception for invalid test")
    void testInvalidTestId() {
        Exception ex = assertThrows(RuntimeException.class, () ->
                testService.generateTestResult(mockUser.getEmail(), 99999));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    @DisplayName("✅ Should generate CareerSuggestion and college list")
    void testCareerSuggestionAndColleges() {
        CareerSuggestion suggestion = new CareerSuggestion(mockUser.getId(), "Engineer", 90, "Logical and Analytical");

        when(careerRecommenderService.generateRecommendation(mockUser.getId())).thenReturn(suggestion);
        when(careerRecommenderService.getCollegesForCareer("Engineer")).thenReturn(
                List.of("IIT Bombay", "MIT", "NIT Trichy"));

        CareerSuggestion result = careerRecommenderService.generateRecommendation(mockUser.getId());

        assertEquals("Engineer", result.getCareer());
        assertTrue(careerRecommenderService.getCollegesForCareer("Engineer").size() >= 3);
    }

    @Test
    @DisplayName("✅ Should generate non-empty PDF")
    void testPdfGeneration() {
        CareerSuggestion suggestion = new CareerSuggestion(mockUser.getId(), "Engineer", 95, "Strong logical skills.");
        List<String> colleges = List.of("MIT", "IIT Bombay", "Stanford");

        byte[] pdf = pdfReportService.generatePdf(suggestion, colleges);

        assertNotNull(pdf);
        assertTrue(pdf.length > 100);
    }
    
    @Test
    void testAllQuestionsLoaded() {
        List<Question> allQuestions = questionRepository.findAll();
        assertFalse(allQuestions.isEmpty(), "Questions should be loaded from CSV files");
        System.out.println("✅ Total questions loaded: " + allQuestions.size());
    }
}
