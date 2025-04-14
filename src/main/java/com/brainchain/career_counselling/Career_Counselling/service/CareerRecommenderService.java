package com.brainchain.career_counselling.Career_Counselling.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.brainchain.career_counselling.Career_Counselling.dto.*;
import com.brainchain.career_counselling.Career_Counselling.entity.*;
import com.brainchain.career_counselling.Career_Counselling.repository.*;

@Service
public class CareerRecommenderService {

    private final TestResultRepository testResultRepository;
    private final CareerRepository careerRepository;
    private final CollegeRepository collegeRepository;

    public CareerRecommenderService(TestResultRepository testResultRepository,
                                    CareerRepository careerRepository,
                                    CollegeRepository collegeRepository) {
        this.testResultRepository = testResultRepository;
        this.careerRepository = careerRepository;
        this.collegeRepository = collegeRepository;
    }

    public UserProfileResult buildUserProfile(Long userId) {
        List<TestResult> results = testResultRepository.findByUserId(userId);
        IQTestResult iqResult = null;
        MBTITestResult mbtiResult = null;
        MITestResult miResult = null;
        RIASECResult riasecResult = null;

        for (TestResult result : results) {
            String testName = result.getTest().getTestName();

            switch (testName) {
                case "IQ Test" -> iqResult = IQTestResult.builder()
                        .score(result.getScore())
                        .category(categorizeIQ(result.getScore()))
                        .build();

                case "MBTI Test" -> {
                    Map<String, Integer> traitCounts = new HashMap<>(Map.of(
                            "E", 0, "I", 0, "S", 0, "N", 0,
                            "T", 0, "F", 0, "J", 0, "P", 0
                    ));

                    for (UserResponse ur : result.getUserResponses()) {
                        Question q = ur.getQuestion();
                        int rating = Integer.parseInt(ur.getUserAnswer());
                        String trait = q.getCorrectAnswer().trim().toUpperCase();

                        if (rating >= 4) {
                            traitCounts.merge(trait, 1, Integer::sum);
                        } else {
                            traitCounts.merge(getOppositeTrait(trait), 1, Integer::sum);
                        }
                    }

                    String mbtiType = "" +
                            (traitCounts.get("E") >= traitCounts.get("I") ? "E" : "I") +
                            (traitCounts.get("S") >= traitCounts.get("N") ? "S" : "N") +
                            (traitCounts.get("T") >= traitCounts.get("F") ? "T" : "F") +
                            (traitCounts.get("J") >= traitCounts.get("P") ? "J" : "P");

                    mbtiResult = MBTITestResult.builder()
                            .mbtiType(mbtiType)
                            .traitCounts(traitCounts)
                            .build();
                }

                case "Multiple Intelligences Test" -> {
                    Map<String, Integer> scores = new HashMap<>();
                    for (UserResponse ur : result.getUserResponses()) {
                        Question q = ur.getQuestion();
                        int rating = Integer.parseInt(ur.getUserAnswer());
                        String intelligence = q.getCorrectAnswer();
                        scores.merge(intelligence, rating, Integer::sum);
                    }

                    List<String> dominant = scores.entrySet().stream()
                            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                            .limit(2)
                            .map(Map.Entry::getKey)
                            .toList();

                    miResult = MITestResult.builder()
                            .intelligenceScores(scores)
                            .dominantIntelligences(dominant)
                            .build();
                }

                case "RIASEC Interest Inventory" -> {
                    Map<String, Integer> categoryScores = new HashMap<>();
                    for (UserResponse ur : result.getUserResponses()) {
                        Question q = ur.getQuestion();
                        int rating = Integer.parseInt(ur.getUserAnswer());
                        String category = q.getCorrectAnswer().toUpperCase();
                        categoryScores.merge(category, rating, Integer::sum);
                    }

                    List<String> top3 = categoryScores.entrySet().stream()
                            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                            .limit(3)
                            .map(Map.Entry::getKey)
                            .toList();

                    String hollandCode = String.join("", top3);

                    riasecResult = RIASECResult.builder()
                            .categoryScores(categoryScores)
                            .hollandCode(hollandCode)
                            .build();
                }
            }
        }

        return UserProfileResult.builder()
                .iqTestResult(iqResult)
                .mbtiTestResult(mbtiResult)
                .miTestResult(miResult)
                .riasecResult(riasecResult)
                .build();
    }

    private String getOppositeTrait(String trait) {
        return switch (trait) {
            case "E" -> "I";
            case "I" -> "E";
            case "S" -> "N";
            case "N" -> "S";
            case "T" -> "F";
            case "F" -> "T";
            case "J" -> "P";
            case "P" -> "J";
            default -> trait;
        };
    }

    private String categorizeIQ(int score) {
        if (score >= 130) return "Very High";
        else if (score >= 120) return "Above Average";
        else if (score >= 90) return "Average";
        else return "Below Average";
    }

    @Cacheable("careerRecommendations")
    public CareerSuggestion generateRecommendation(Long userId) {
        UserProfileResult profile = buildUserProfile(userId);
        return generateRecommendation(profile).toBuilder().userId(userId).build();
    }

    public CareerSuggestion generateRecommendationFromProfile(UserProfileResult profile) {
        return generateRecommendation(profile);
    }

    private CareerSuggestion generateRecommendation(UserProfileResult profile) {
        String mbtiType = profile.getMbtiTestResult() != null ? profile.getMbtiTestResult().getMbtiType() : "";
        String miTag = profile.getMiTestResult() != null ? profile.getMiTestResult().getDominantIntelligences().get(0) : "";
        String riasec = profile.getRiasecResult() != null ? profile.getRiasecResult().getHollandCode() : "";
        String iq = profile.getIqTestResult() != null ? profile.getIqTestResult().getCategory() : "";

        List<Career> allCareers = careerRepository.findAll();

        Career matchedCareer = allCareers.stream()
                .filter(c -> containsIgnoreCase(c.getMbtiTypes(), mbtiType))
                .filter(c -> containsIgnoreCase(c.getMiTags(), miTag))
                .filter(c -> containsIgnoreCase(c.getRiasecCodes(), riasec))
                .filter(c -> containsIgnoreCase(c.getIqLevels(), iq))
                .findFirst()
                .orElse(new Career());

        String careerName = matchedCareer.getCareerName() != null ? matchedCareer.getCareerName() : "Generalist";
        List<String> colleges = getCollegesForCareer(careerName);

        return CareerSuggestion.builder()
                .userId(null)
                .career(careerName)
                .score(profile.getIqTestResult() != null ? profile.getIqTestResult().getScore() : 0)
                .summary("Based on your profile, a suitable career is: " + careerName + ". Top colleges: " + String.join(", ", colleges))
                .build();
    }

    private boolean containsIgnoreCase(String csv, String value) {
        if (csv == null || value == null) return false;
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .anyMatch(val -> val.equalsIgnoreCase(value));
    }

    public List<String> getCollegesForCareer(String career) {
        return collegeRepository.findByCareerCareerNameIgnoreCase(career)
                .stream().map(College::getCollegeName).collect(Collectors.toList());
    }

    public String generateCareerReport(UserProfileResult profile) {
        CareerSuggestion suggestion = generateRecommendationFromProfile(profile);
        return suggestion.getSummary();
    }
}