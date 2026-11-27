package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidAvatarException extends BaseException {
    public InvalidAvatarException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
