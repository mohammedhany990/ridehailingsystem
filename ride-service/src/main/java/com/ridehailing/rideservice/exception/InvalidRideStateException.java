package com.ridehailing.rideservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidRideStateException extends BaseException{
    public InvalidRideStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
