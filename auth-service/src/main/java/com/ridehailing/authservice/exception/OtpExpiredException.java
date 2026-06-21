package com.ridehailing.authservice.exception;

import org.springframework.http.HttpStatus;

public class OtpExpiredException extends BaseException{

    public OtpExpiredException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }
}
