package com.brainchain.career_counselling.Career_Counselling.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "OTP is required")
    private String otp;
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String newPassword;
    @NotBlank(message = "Confirm password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String confirmPassword;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return newPassword != null && confirmPassword != null && newPassword.equals(confirmPassword);
    }
}
