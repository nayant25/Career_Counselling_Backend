package com.brainchain.career_counselling.Career_Counselling.exceptions;

public class InternalServerException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public InternalServerException(String message) {
        super(message);
    }
}
