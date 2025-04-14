package com.brainchain.career_counselling.Career_Counselling.Controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.brainchain.career_counselling.Career_Counselling.dto.AnswerRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.CareerSuggestion;
import com.brainchain.career_counselling.Career_Counselling.dto.TestResponse;
import com.brainchain.career_counselling.Career_Counselling.entity.TestResult;
import com.brainchain.career_counselling.Career_Counselling.entity.User;
import com.brainchain.career_counselling.Career_Counselling.repository.TestResultRepository;
import com.brainchain.career_counselling.Career_Counselling.service.CareerRecommenderService;
import com.brainchain.career_counselling.Career_Counselling.service.PdfReportService;
import com.brainchain.career_counselling.Career_Counselling.service.ReportService;
import com.brainchain.career_counselling.Career_Counselling.service.TestService;
import com.brainchain.career_counselling.Career_Counselling.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/test")
public class TestController {

	private final TestService testService;
	private final CareerRecommenderService recommendationService;
	private final ReportService reportService;
	private final UserService userService;
	private final TestResultRepository testResultRepository;
	private final PdfReportService pdfReportService;

	public TestController(TestService testService,
						  CareerRecommenderService recommendationService,
						  ReportService reportService,
						  UserService userService,
						  TestResultRepository testResultRepository,
						  PdfReportService pdfReportService) {
		this.testService = testService;
		this.recommendationService = recommendationService;
		this.reportService = reportService;
		this.userService = userService;
		this.testResultRepository = testResultRepository;
		this.pdfReportService = pdfReportService;
	}

	@GetMapping("/start/{testId}")
	public ResponseEntity<TestResponse> startTest(@PathVariable Integer testId, Authentication authentication) {
		String userId = authentication.getName(); // email
		TestResponse response = testService.startTest(userId, testId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/submit")
	public ResponseEntity<TestResponse> submitAnswer(@Valid @RequestBody AnswerRequest request,
													 Authentication authentication) {
		String userEmail = authentication.getName();
		User user = userService.getUserByEmail(userEmail);
		TestResponse response = testService.submitAnswer(request);

		if (response.getNextQuestion() == null && response.getMessage().contains("completed")) {
			List<TestResult> results = testResultRepository.findByUserId(user.getId());
			long completedCount = results.stream().filter(r -> r.getStatus() == TestResult.Status.COMPLETED).count();

			if (completedCount == 4) {
				CareerSuggestion suggestion = recommendationService.generateRecommendation(user.getId());
				String report = reportService.generateReport(results, suggestion);
				response.setMessage(response.getMessage() + "\n\n" + report);
			} else {
				// Partial report — only show scores of completed tests
				String partialReport = reportService.generateReport(results, null);
				response.setMessage(response.getMessage() + "\n\n" + partialReport);
			}
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("/pause/{testId}")
	public ResponseEntity<TestResult> pauseTest(@PathVariable Integer testId, Authentication authentication) {
		String userId = authentication.getName();
		TestResult result = testService.pauseTest(userId, testId);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/retake/{testId}")
	public ResponseEntity<TestResponse> retakeTest(@PathVariable Integer testId, Authentication authentication) {
		String userId = authentication.getName();
		TestResponse response = testService.retakeTest(userId, testId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/complete/{testId}")
	public ResponseEntity<TestResult> completeTest(@PathVariable Integer testId, Authentication authentication) {
		String userId = authentication.getName();
		TestResult result = testService.generateTestResult(userId, testId);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/report/download")
	public ResponseEntity<byte[]> downloadPdfReport(Authentication authentication) {
		String userEmail = authentication.getName();
		User user = userService.getUserByEmail(userEmail);

		List<TestResult> userResults = testResultRepository.findByUserId(user.getId());
		long completedCount = userResults.stream().filter(r -> r.getStatus() == TestResult.Status.COMPLETED).count();

		if (completedCount < 4) {
			return ResponseEntity.badRequest()
					.body("Complete all 4 tests to download the career report.".getBytes());
		}

		CareerSuggestion suggestion = recommendationService.generateRecommendation(user.getId());
		List<String> colleges = recommendationService.getCollegesForCareer(suggestion.getCareer());
		byte[] pdfBytes = pdfReportService.generatePdf(suggestion, colleges);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=career_report.pdf")
				.contentType(MediaType.APPLICATION_PDF)
				.body(pdfBytes);
	}
}
