package com.brainchain.career_counselling.Career_Counselling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CareerSuggestion {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotBlank(message = "Career suggestion is required")
    private String career;
    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score cannot be negative")
    private Integer score;
    @NotBlank(message = "Summary is required")
    private String summary;
}
