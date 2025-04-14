package com.brainchain.career_counselling.Career_Counselling.exceptions;

public class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public BadRequestException(String message) {
        super(message);
    }
}
