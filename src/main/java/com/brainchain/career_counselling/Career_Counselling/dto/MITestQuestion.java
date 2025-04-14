package com.brainchain.career_counselling.Career_Counselling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MITestQuestion {
    private int questionId;
    private String intelligenceType; // E.g., Logical, Musical, etc.
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}

