package com.brainchain.career_counselling.Career_Counselling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Pattern(regexp = "^\\+[1-9]\\d{9,14}$", message = "Phone number must be in E.164 format (e.g., +1234567890)")
    private String phoneNumber;
}