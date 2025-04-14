package com.brainchain.career_counselling.Career_Counselling.Controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.brainchain.career_counselling.Career_Counselling.dto.AuthResponse;
import com.brainchain.career_counselling.Career_Counselling.dto.EmailLoginRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.ForgotPasswordRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.OtpVerificationRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.ResetPasswordRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.SignupRequest;
import com.brainchain.career_counselling.Career_Counselling.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        String response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email-login")
    public ResponseEntity<AuthResponse> emailLogin(@Valid @RequestBody EmailLoginRequest request) {
        AuthResponse response = authService.loginWithEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
}