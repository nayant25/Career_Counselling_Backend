package com.brainchain.career_counselling.Career_Counselling.dto;

import lombok.Data;

@Data
public class IQQuestion {
    private int questionId;
    private int testId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
}
