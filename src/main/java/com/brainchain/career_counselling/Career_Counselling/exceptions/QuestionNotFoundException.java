package com.brainchain.career_counselling.Career_Counselling.exceptions;

public class QuestionNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public QuestionNotFoundException(String message) {
        super(message);
    }
    public QuestionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
