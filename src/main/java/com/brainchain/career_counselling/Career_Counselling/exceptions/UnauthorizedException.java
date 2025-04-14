package com.brainchain.career_counselling.Career_Counselling.exceptions;

public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public UnauthorizedException(String message) {
        super(message);
    }
}
