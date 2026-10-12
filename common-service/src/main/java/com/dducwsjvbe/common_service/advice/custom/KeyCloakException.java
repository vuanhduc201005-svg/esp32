package com.dducwsjvbe.common_service.advice.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class KeyCloakException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    public KeyCloakException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
