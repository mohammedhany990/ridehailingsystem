package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidGoogleTokenException extends BaseException {
    public InvalidGoogleTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
