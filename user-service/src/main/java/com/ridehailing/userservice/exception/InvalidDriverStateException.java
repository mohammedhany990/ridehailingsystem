package com.ridehailing.userservice.exception;


import org.springframework.http.HttpStatus;

public class InvalidDriverStateException extends BaseException {

    public InvalidDriverStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}