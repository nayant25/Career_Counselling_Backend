package com.brainchain.career_counselling.Career_Counselling.service;

import java.security.SecureRandom;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Add this import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.brainchain.career_counselling.Career_Counselling.dto.AuthResponse;
import com.brainchain.career_counselling.Career_Counselling.dto.EmailLoginRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.OtpVerificationRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.PhoneLoginRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.SignupRequest;
import com.brainchain.career_counselling.Career_Counselling.entity.User;
import com.brainchain.career_counselling.Career_Counselling.exceptions.BadRequestException;
import com.brainchain.career_counselling.Career_Counselling.exceptions.NotFoundException;
import com.brainchain.career_counselling.Career_Counselling.exceptions.UnauthorizedException;
import com.brainchain.career_counselling.Career_Counselling.repository.UserRepository;
import com.brainchain.career_counselling.Career_Counselling.util.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String signup(SignupRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().matches("^\\+[1-9]\\d{9,14}$")) {
            throw new BadRequestException("Phone number must be in E.164 format (e.g., +1234567890)");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }
        if (request.getPhoneNumber() != null && userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new BadRequestException("Phone Number already Registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(request.getPassword()); // Already hashed via setter in User
        user.setRole(User.Role.STUDENT);

        String emailOtp = generateOtp();
        user.setEmailOtp(emailOtp);

        sendEmailOtp(user.getEmail(), emailOtp);
        userRepository.save(user);
        return "Signup successful. Verify your email with OTP.";
    }

    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with Email: " + request.getEmail()));

        if (!request.getOtp().equals(user.getEmailOtp())) {
            throw new UnauthorizedException("Invalid OTP");
        }

        user.setEmailVerified(true);
        user.setEmailOtp(null);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        return new AuthResponse(token, user.getRole().name(), user.getEmail());
    }

    public AuthResponse loginWithEmail(EmailLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: " + request.getEmail()));

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email not verified");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid Credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        return new AuthResponse(token, user.getRole().name(), user.getEmail());
    }

    public AuthResponse loginWithPhone(PhoneLoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new NotFoundException("User not found with phone number: " + request.getPhoneNumber()));

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email not verified");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        return new AuthResponse(token, user.getRole().name(), user.getEmail());
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(999999));
    }

    private void sendEmailOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Career Counselling OTP Verification");
        message.setText("Your OTP is: " + otp);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Email service unavailable", e);
        }
    }
}