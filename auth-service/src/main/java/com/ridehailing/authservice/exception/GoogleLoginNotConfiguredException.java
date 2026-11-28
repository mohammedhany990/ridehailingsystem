package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class GoogleLoginNotConfiguredException extends BaseException {
    public GoogleLoginNotConfiguredException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
