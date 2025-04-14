package com.brainchain.career_counselling.Career_Counselling.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.brainchain.career_counselling.Career_Counselling.dto.AuthResponse;
import com.brainchain.career_counselling.Career_Counselling.dto.EmailLoginRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.ForgotPasswordRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.OtpVerificationRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.ResetPasswordRequest;
import com.brainchain.career_counselling.Career_Counselling.dto.SignupRequest;
import com.brainchain.career_counselling.Career_Counselling.entity.User;
import com.brainchain.career_counselling.Career_Counselling.exceptions.BadRequestException;
import com.brainchain.career_counselling.Career_Counselling.exceptions.InternalServerException;
import com.brainchain.career_counselling.Career_Counselling.exceptions.NotFoundException;
import com.brainchain.career_counselling.Career_Counselling.exceptions.UnauthorizedException;
import com.brainchain.career_counselling.Career_Counselling.repository.UserRepository;
import com.brainchain.career_counselling.Career_Counselling.util.JwtUtil;
import java.util.Collections;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_NOT_FOUND_MSG = "User not found with email: ";

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, JavaMailSender mailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public String signup(SignupRequest request) {
        validateSignupRequest(request);
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new BadRequestException("Phone number already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.STUDENT);
        user.setEmailVerified(false);
        String emailOtp = generateOtp();
        user.setOtp(emailOtp);
        userRepository.save(user);
        sendEmailOtp(user.getEmail(), emailOtp);
        return "Signup successful. Verify your email with OTP.";
    }

    public AuthResponse verifyOtp(OtpVerificationRequest request) {
        validateOtpRequest(request);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MSG + request.getEmail()));
        if (!request.getOtp().equals(user.getOtp())) {
            throw new UnauthorizedException("Invalid OTP");
        }
        user.setEmailVerified(true);
        user.setOtp(null);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse loginWithEmail(EmailLoginRequest request) {
        validateLoginRequest(request);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MSG + request.getEmail()));
        if (!user.getEmailVerified()) {
            throw new UnauthorizedException("Email not verified");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MSG + request.getEmail()));
        String resetOtp = generateOtp();
        user.setResetPasswordOtp(resetOtp);
        user.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        sendResetOtpEmail(user.getEmail(), resetOtp);
        return "Password reset OTP sent to " + request.getEmail();
    }

    public String resetPassword(ResetPasswordRequest request) {
        validateResetPasswordRequest(request);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_MSG + request.getEmail()));
        if (!request.getOtp().equals(user.getResetPasswordOtp()) ||
            user.getResetPasswordExpiry() == null ||
            user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordOtp(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);
        return "Password reset successfully";
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(999999));
    }

    private void sendEmailOtp(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Career Counselling OTP Verification");
            message.setText("Your OTP is: " + otp);
            mailSender.send(message);
        } catch (Exception e) {
            throw new InternalServerException("Failed to send OTP email: " + e.getMessage());
        }
    }

    private void sendResetOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Career Counselling Password Reset OTP");
            message.setText("Your password reset OTP is: " + otp + "\nValid for 15 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            throw new InternalServerException("Failed to send reset OTP email: " + e.getMessage());
        }
    }

    private void validateSignupRequest(SignupRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty())
            throw new BadRequestException("Email is required");
        if (request.getPassword() == null || request.getPassword().trim().isEmpty())
            throw new BadRequestException("Password is required");
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty())
            throw new BadRequestException("Confirm password is required");
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException("Passwords do not match");
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())
            throw new BadRequestException("Phone number is required");
        if (request.getName() == null || request.getName().trim().isEmpty())
            throw new BadRequestException("Name is required");
    }

    private void validateOtpRequest(OtpVerificationRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty())
            throw new BadRequestException("Email is required");
        if (request.getOtp() == null || request.getOtp().trim().isEmpty())
            throw new BadRequestException("OTP is required");
    }

    private void validateLoginRequest(EmailLoginRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty())
            throw new BadRequestException("Email is required");
        if (request.getPassword() == null || request.getPassword().trim().isEmpty())
            throw new BadRequestException("Password is required");
    }

    private void validateResetPasswordRequest(ResetPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty())
            throw new BadRequestException("Email is required");
        if (request.getOtp() == null || request.getOtp().trim().isEmpty())
            throw new BadRequestException("OTP is required");
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty())
            throw new BadRequestException("New password is required");
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty())
            throw new BadRequestException("Confirm password is required");
    }
}
