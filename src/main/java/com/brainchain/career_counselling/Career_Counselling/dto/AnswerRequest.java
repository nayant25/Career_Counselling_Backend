package com.brainchain.career_counselling.Career_Counselling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotNull(message = "Test ID is required")
    private Integer testId;
    @NotNull(message = "Question ID is required")
    private Long questionId;
    @NotBlank(message = "User answer is required")
    private String userAnswer;
}
