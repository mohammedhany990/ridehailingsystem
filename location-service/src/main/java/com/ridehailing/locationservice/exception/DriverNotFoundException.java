package com.ridehailing.locationservice.exception;

import org.springframework.http.HttpStatus;

public class DriverNotFoundException extends BaseException {

    public DriverNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
