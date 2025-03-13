package com.brainchain.career_counselling.Career_Counselling.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brainchain.career_counselling.Career_Counselling.dto.AuthResponse;
import com.brainchain.career_counselling.Career_Counselling.dto.EmailLoginRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.OtpVerificationRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.PhoneLoginRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.SignupRequest;
import com.brainchain.career_counselling.Career_Counselling.service.AuthService;
import com.brainchain.career_counselling.Career_Counselling.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AuthService authService;

	@GetMapping("/success")
	public ResponseEntity<AuthResponse> authSuccess(Authentication authentication) {
		String email = authentication.getName();
		String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
		String token = jwtUtil.generateToken(email, authentication.getAuthorities());
		return ResponseEntity.ok(new AuthResponse(token, role, email));

	}

	@PostMapping("/signup")
    public ResponseEntity<String> signup( @Valid @RequestBody SignupRequest request) {
		System.out.println("Received Signup Request: " + request);
        return ResponseEntity.ok(authService.signup(request));
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request){
		return ResponseEntity.ok(authService.verifyOtp(request));
	}
	
	@PostMapping("/email-login")
	public ResponseEntity<AuthResponse> emailLogin(@Valid @RequestBody EmailLoginRequest request){
		return ResponseEntity.ok(authService.loginWithEmail(request));
	}
	
	@PostMapping("/phone-login")
	public ResponseEntity<AuthResponse> phoneLogin(@Valid @RequestBody PhoneLoginRequest request){
		return ResponseEntity.ok(authService.loginWithPhone(request));
	}
	
	
}
