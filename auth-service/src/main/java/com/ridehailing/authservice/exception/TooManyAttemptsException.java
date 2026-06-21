package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class TooManyAttemptsException extends BaseException{
    public TooManyAttemptsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
