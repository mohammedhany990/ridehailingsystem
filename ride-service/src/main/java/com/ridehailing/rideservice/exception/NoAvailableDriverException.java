package com.ridehailing.rideservice.exception;

import org.springframework.http.HttpStatus;

public class NoAvailableDriverException extends BaseException {
    public NoAvailableDriverException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
