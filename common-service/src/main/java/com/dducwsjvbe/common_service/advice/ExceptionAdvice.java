package com.dducwsjvbe.common_service.advice;

import com.dducwsjvbe.common_service.advice.custom.KeyCloakException;
import com.dducwsjvbe.common_service.advice.custom.NotFoundException;
import com.dducwsjvbe.common_service.advice.custom.RefreshTokenExpiredException;
import com.dducwsjvbe.common_service.model.ErrorMessage;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;


@ControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleException(Exception e) {
        return new ResponseEntity<>(
                new ErrorMessage(
                        "500",
                        e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorMessage> handleRuntimeException(RuntimeException e) {
        return new ResponseEntity<>(
                new ErrorMessage(
                        "400",
                        e.getMessage(),
                        HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> handleException(NotFoundException e) {
        return new ResponseEntity<>(
                new ErrorMessage(
                        "404",
                        e.getMessage(),
                        HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(KeyCloakException.class)
    public ResponseEntity<ErrorMessage> handleException(KeyCloakException e) {
        return new ResponseEntity<>(
                new ErrorMessage(
                        e.getCode(),
                        e.getMessage(),
                        e.getStatus()),
                e.getStatus());
    }
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorMessage> handleException(RefreshTokenExpiredException e) {
        return new ResponseEntity<>(
                new ErrorMessage(
                        "401",
                        e.getMessage(),
                        HttpStatus.UNAUTHORIZED),
                HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorMessage> handleException(IOException e) {
        return new ResponseEntity<>(
                new ErrorMessage(
                        "400",
                        e.getMessage(),
                        HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleException(IllegalArgumentException e) {
        return new ResponseEntity<>(
                new ErrorMessage(
                        "400",
                        e.getMessage(),
                        HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }
}


