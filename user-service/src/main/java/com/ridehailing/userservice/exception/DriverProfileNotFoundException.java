package com.ridehailing.userservice.exception;

import org.springframework.http.HttpStatus;

public class DriverProfileNotFoundException extends BaseException{
    public DriverProfileNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
