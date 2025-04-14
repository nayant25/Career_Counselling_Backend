package com.brainchain.career_counselling.Career_Counselling.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brainchain.career_counselling.Career_Counselling.assesments.IQTest;
import com.brainchain.career_counselling.Career_Counselling.assesments.MBTITest;
import com.brainchain.career_counselling.Career_Counselling.assesments.MultipleIntelligencesTest;
import com.brainchain.career_counselling.Career_Counselling.assesments.RIASECTest;
import com.brainchain.career_counselling.Career_Counselling.dto.UserProfileResult;
import com.brainchain.career_counselling.Career_Counselling.service.CareerRecommenderService;

@RestController
public class CareerCounsellingController {

    private final IQTest iqTest;
    private final MultipleIntelligencesTest miTest;
    private final RIASECTest riasecTest;
    private final MBTITest mbtiTest;
    private final CareerRecommenderService recommenderService;

    public CareerCounsellingController(IQTest iqTest,
                                       MultipleIntelligencesTest miTest,
                                       RIASECTest riasecTest,
                                       MBTITest mbtiTest,
                                       CareerRecommenderService recommenderService) {
        this.iqTest = iqTest;
        this.miTest = miTest;
        this.riasecTest = riasecTest;
        this.mbtiTest = mbtiTest;
        this.recommenderService = recommenderService;
    }

    @GetMapping("/career-report")
    public String getCareerReport() {
        // Load questions from CSV files (if not already loaded on startup)
        iqTest.loadQuestions();
        miTest.loadQuestions();
        riasecTest.loadQuestions();
        mbtiTest.loadQuestions();

        // In a production system, user responses would be captured via POST endpoints or sessions.
        // Here, we simulate them (for testing/demo) by setting sample responses:

        // For the IQ test – assume the user selects options that yield a score of 22.
        iqTest.setUserResponses(java.util.Arrays.asList("A", "B", "C", "D", "A", "C", "B", "A", "D", "B")); 
        // (Extend the list as needed to match the number of questions.)

        // For the MI test, simulate responses (rating from 1 to 5):
        miTest.setUserResponse(1, 4);  // For questionId 1, rating = 4, etc.
        miTest.setUserResponse(2, 3);
        // (Simulate for additional questions...)

        // For the RIASEC test:
        riasecTest.setUserResponse(1, 4);
        riasecTest.setUserResponse(2, 2);
        // (Set more responses as needed.)

        // For the MBTI test:
        mbtiTest.setUserResponse(1, 5);
        mbtiTest.setUserResponse(2, 2);
        // (Set more responses as needed.)

        // Calculate results for each test
        var iqResult = iqTest.calculateResult();
        var miResult = miTest.calculateResult();
        var riasecResult = riasecTest.calculateResult();
        var mbtiResult = mbtiTest.calculateResult();
        
        // Aggregate results into a unified profile
        UserProfileResult profile = new UserProfileResult();
        profile.setIqTestResult(iqResult);
        profile.setMiTestResult(miResult);
        profile.setRiasecResult(riasecResult);
        profile.setMbtiTestResult(mbtiResult);
        
        // Generate and return the final career report
        return recommenderService.generateCareerReport(profile);
    }
}
