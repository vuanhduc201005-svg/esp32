package com.dducwsjvbe.common_service.advice.custom;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
