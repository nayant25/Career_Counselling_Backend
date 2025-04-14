package com.brainchain.career_counselling.Career_Counselling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    @NotNull(message = "Question ID is required")
    private Long questionId;
    @NotNull(message = "Test ID is required")
    private Integer testId;
    @NotBlank(message = "Question text is required")
    private String question;
    @NotBlank(message = "Option A is required")
    private String optionA;
    @NotBlank(message = "Option B is required")
    private String optionB;
    private String optionC;
    private String optionD;

    private String correctAnswer;
    public List<String> getOptions() {
        return Stream.of(optionA, optionB, optionC, optionD)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    public int getCorrectAnswerIndex() {
        List<String> options = getOptions();
        int index = options.indexOf(correctAnswer);
        if (index == -1) {
            throw new IllegalStateException("Correct answer '" + correctAnswer + "' not found in options: " + options);
        }
        return index;
    }
}
