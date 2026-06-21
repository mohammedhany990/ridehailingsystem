package com.ridehailing.rideservice.exception;

import org.springframework.http.HttpStatus;

public class RideNotFoundException extends BaseException {
    public RideNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}