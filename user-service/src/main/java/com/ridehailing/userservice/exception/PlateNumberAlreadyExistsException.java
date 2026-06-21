package com.ridehailing.userservice.exception;

import org.springframework.http.HttpStatus;

public class PlateNumberAlreadyExistsException extends BaseException{
    public PlateNumberAlreadyExistsException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
    }
}
