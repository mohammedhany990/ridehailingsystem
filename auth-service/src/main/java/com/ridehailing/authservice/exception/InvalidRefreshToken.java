package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshToken  extends BaseException {
    public InvalidRefreshToken(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
