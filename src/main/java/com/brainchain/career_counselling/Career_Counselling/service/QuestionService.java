package com.brainchain.career_counselling.Career_Counselling.service;

import com.brainchain.career_counselling.Career_Counselling.entity.CareerTest;
import com.brainchain.career_counselling.Career_Counselling.entity.Question;
import com.brainchain.career_counselling.Career_Counselling.repository.CareerTestRepository;
import com.brainchain.career_counselling.Career_Counselling.repository.QuestionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Profile("!test")
@Service
public class QuestionService implements ApplicationRunner {

    private final QuestionRepository questionRepository;
    private final CareerTestRepository careerTestRepository;

    public QuestionService(QuestionRepository questionRepository,
                           CareerTestRepository careerTestRepository) {
        this.questionRepository = questionRepository;
        this.careerTestRepository = careerTestRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (shouldSkipCsvLoading(args)) {
            System.out.println("⚠️ Skipping question CSV loading due to test profile or skip flag.");
            return;
        }

        Map<String, String> csvTestMapping = Map.of(
                "RIASEC_Interest_Inventory_Questions.csv", "RIASEC Interest Inventory",
                "Gartner_Multiple_Intelligences_Test.csv", "Multiple Intelligences Test",
                "youth_friendly_mbti_test.csv", "MBTI Test",
                "IQ_Test_Questions.csv", "IQ Test"
        );

        for (Map.Entry<String, String> entry : csvTestMapping.entrySet()) {
            String csvFile = entry.getKey();
            String testName = entry.getValue();

            CareerTest test = careerTestRepository.findByTestName(testName).orElseGet(() -> {
                CareerTest newTest = new CareerTest();
                newTest.setTestName(testName);
                newTest.setDescription(getTestDescription(testName));
                newTest.setStatus(CareerTest.Status.ACTIVE);
                newTest.setValidateAnswer("IQ Test".equals(testName));
                newTest.setCreatedAt(LocalDateTime.now());
                newTest.setUpdatedAt(LocalDateTime.now());
                return careerTestRepository.save(newTest);
            });

            Resource resource = new ClassPathResource(csvFile);
            if (!resource.exists()) {
                System.err.println("❌ CSV file not found: " + csvFile);
                continue;
            }

            List<Question> questions = new ArrayList<>();
            int totalCount = 0, skippedCount = 0;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean firstLine = true;
                while ((line = br.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }

                    totalCount++;
                    String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handle quoted fields

                    if (data.length < 7) {
                        System.out.printf("⛔ Skipping malformed row (only %d columns) at line %d: %s%n", data.length, totalCount + 1, line);
                        skippedCount++;
                        continue;
                    }

                    String trait = getSafe(data, 1); // Used for RIASEC, MI, MBTI
                    String questionText = getSafe(data, 2);
                    String optionA = getSafe(data, 3);
                    String optionB = getSafe(data, 4);
                    String optionC = getSafe(data, 5);
                    String optionD = getSafe(data, 6);
                    String correctKey = (data.length > 7) ? getSafe(data, 7).toUpperCase() : ""; // Used for IQ Test

                    // Basic validation for all tests
                    if (questionText.isEmpty()) {
                        System.out.printf("⛔ Skipping row at line %d (missing question): %s%n", totalCount + 1, line);
                        skippedCount++;
                        continue;
                    }

                    Question question = new Question();
                    question.setTest(test);
                    question.setQuestion(questionText);
                    question.setOptionA(optionA);
                    question.setOptionB(optionB);
                    question.setOptionC(optionC);
                    question.setOptionD(optionD);

                    if ("IQ Test".equals(testName)) {
                        // IQ Test specific logic
                        if (!isValidQuestion(questionText, correctKey, optionA, optionB, optionC, optionD)) {
                            System.out.printf("⛔ Skipping invalid IQ question at line %d: Q: %s | A: %s | B: %s | C: %s | D: %s | Key: %s%n",
                                    totalCount + 1, questionText, optionA, optionB, optionC, optionD, correctKey);
                            skippedCount++;
                            continue;
                        }
                        question.setCorrectAnswer(correctKey); // Set to 'A', 'B', 'C', or 'D'
                    } else {
                        // RIASEC, MI, MBTI: Use trait/category as correctAnswer
                        if (trait.isEmpty()) {
                            System.out.printf("⛔ Skipping %s row at line %d (missing trait/category): %s%n", testName, totalCount + 1, line);
                            skippedCount++;
                            continue;
                        }
                        question.setCorrectAnswer(trait);
                    }

                    questions.add(question);
                }
            }

            if (!questions.isEmpty()) {
                questionRepository.saveAll(questions);
                System.out.printf("✅ Loaded %d / %d questions for '%s'. Skipped: %d%n",
                        questions.size(), totalCount, testName, skippedCount);
            } else {
                System.out.printf("⚠️ No valid questions loaded from: %s%n", csvFile);
            }
        }
    }

    private String getTestDescription(String testName) {
        return switch (testName) {
            case "RIASEC Interest Inventory" -> "Explore your vocational interests using the Holland Code model.";
            case "Multiple Intelligences Test" -> "Assess your Gardner's multiple intelligences.";
            case "MBTI Test" -> "Discover your personality type.";
            case "IQ Test" -> "Measure your cognitive abilities.";
            default -> "Auto-loaded test for " + testName;
        };
    }

    private String getSafe(String[] data, int index) {
        return index < data.length ? data[index].trim().replaceAll("^\"|\"$", "") : "";
    }

    private boolean shouldSkipCsvLoading(ApplicationArguments args) {
        return args.containsOption("spring.profiles.active") &&
                args.getOptionValues("spring.profiles.active").contains("test") ||
                args.containsOption("skip.csv.loading") &&
                args.getOptionValues("skip.csv.loading").contains("true");
    }

    private boolean isValidQuestion(String question, String answer, String a, String b, String c, String d) {
        if (question == null || question.trim().isEmpty()) {
            System.out.println("❌ Rejected: Question is empty");
            return false;
        }
        if (answer == null || answer.trim().isEmpty()) {
            System.out.println("❌ Rejected: Answer is empty");
            return false;
        }
        answer = answer.trim().toUpperCase();
        if (!List.of("A", "B", "C", "D").contains(answer)) {
            System.out.println("❌ Rejected: Invalid answer key: " + answer);
            return false;
        }
        String option = switch (answer) {
            case "A" -> a;
            case "B" -> b;
            case "C" -> c;
            case "D" -> d;
            default -> null;
        };
        if (option == null || option.trim().isEmpty()) {
            System.out.println("❌ Rejected: Option " + answer + " is empty");
            return false;
        }
        return true;
    }
}