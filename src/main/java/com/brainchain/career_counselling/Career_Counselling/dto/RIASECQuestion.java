package com.brainchain.career_counselling.Career_Counselling.dto;

import lombok.Data;

@Data
public class RIASECQuestion {
    private int questionId;
    private String category; // R, I, A, S, E, C
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}
