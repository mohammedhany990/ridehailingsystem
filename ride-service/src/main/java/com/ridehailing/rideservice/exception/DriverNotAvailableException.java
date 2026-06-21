package com.ridehailing.rideservice.exception;

import org.springframework.http.HttpStatus;

public class DriverNotAvailableException extends BaseException {
    public DriverNotAvailableException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
