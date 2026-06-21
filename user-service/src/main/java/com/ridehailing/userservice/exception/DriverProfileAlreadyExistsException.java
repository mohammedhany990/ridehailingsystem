package com.ridehailing.userservice.exception;

import org.springframework.http.HttpStatus;

public class DriverProfileAlreadyExistsException extends BaseException {
    public DriverProfileAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
