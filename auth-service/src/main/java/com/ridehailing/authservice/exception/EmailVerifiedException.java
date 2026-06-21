package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class EmailVerifiedException extends BaseException{
    public EmailVerifiedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
