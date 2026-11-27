package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationRequiredException extends BaseException {
    public AuthenticationRequiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
