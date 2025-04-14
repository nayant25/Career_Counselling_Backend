package com.brainchain.career_counselling.Career_Counselling.dto;

import lombok.Data;

@Data
public class MBTIQuestion {
    private int questionId;
    private String traitDirection; // e.g., "E" or "S"
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}
