package com.ridehailing.userservice.exception;

import org.springframework.http.HttpStatus;

public class LicenseNumberAlreadyExistsException extends BaseException {
    public LicenseNumberAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
