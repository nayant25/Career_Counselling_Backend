package com.brainchain.career_counselling.Career_Counselling.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}