package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidRoleException extends BaseException {
    public InvalidRoleException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
