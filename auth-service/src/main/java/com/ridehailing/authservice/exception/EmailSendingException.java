package com.ridehailing.authservice.exception;


import org.springframework.http.HttpStatus;

public class EmailSendingException extends BaseException {
    public EmailSendingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}