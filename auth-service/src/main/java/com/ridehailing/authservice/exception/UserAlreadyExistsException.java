package com.ridehailing.authservice.exception;


import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}