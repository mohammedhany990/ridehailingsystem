package com.ridehailing.authservice.exception;

import com.ridehailing.authservice.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends BaseException {

    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}