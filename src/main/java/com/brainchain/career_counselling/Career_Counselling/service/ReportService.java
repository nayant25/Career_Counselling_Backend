package com.brainchain.career_counselling.Career_Counselling.service;

import com.brainchain.career_counselling.Career_Counselling.dto.CareerSuggestion;
import com.brainchain.career_counselling.Career_Counselling.entity.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);

    private final CareerRecommenderService recommendationService;

    public ReportService(CareerRecommenderService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public String generateReport(List<TestResult> results, CareerSuggestion suggestion) {
        validateInputs(results, suggestion);

        long completedCount = results.stream()
                .filter(r -> r.getStatus() == TestResult.Status.COMPLETED)
                .count();

        boolean isFull = results.stream()
                .map(r -> r.getTest().getTestName())
                .distinct()
                .count() == 4;

        StringBuilder report = new StringBuilder();
        report.append("========== Unified Career Report ==========\n\n");

        report.append("User ID: ").append(suggestion.getUserId()).append("\n")
              .append("Career Recommendation: ").append(suggestion.getCareer()).append("\n")
              .append("Summary: ").append(suggestion.getSummary()).append("\n\n");

        report.append(">>> Test Results:\n");
        for (TestResult result : results) {
            report.append("Test: ").append(result.getTest().getTestName()).append("\n")
                  .append("Score: ").append(result.getScore()).append("\n")
                  .append("Status: ").append(result.getStatus()).append("\n\n");
        }

        if (isFull) {
            List<String> colleges = recommendationService.getCollegesForCareer(suggestion.getCareer());
            report.append(">>> Suggested Colleges for '").append(suggestion.getCareer()).append("':\n")
                  .append(colleges.stream().map(college -> "- " + college).collect(Collectors.joining("\n")))
                  .append("\n");
        } else {
            report.append("❗ Complete all 4 tests to unlock full career + college recommendation.\n");
        }

        report.append("\n===========================================\n");

        LOGGER.info("Generated Report:\n{}", report);
        return report.toString();
    }

    private void validateInputs(List<TestResult> results, CareerSuggestion suggestion) {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("Test results cannot be null or empty.");
        }
        if (results.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("One or more test results are null.");
        }
        if (suggestion == null) {
            throw new IllegalArgumentException("Career suggestion cannot be null.");
        }
    }
}
