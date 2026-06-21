package com.ridehailing.authservice.exception;

import com.ridehailing.authservice.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex) {

        log.warn("Handled exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());

        return buildResponse(ex.getStatus(), ex.getMessage());
    }


    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabled(DisabledException ex) {

        log.warn("DisabledException: {}", ex.getMessage());

        return buildResponse(HttpStatus.UNAUTHORIZED, "Please verify your email first");
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {

        log.warn("BadCredentialsException: {}", ex.getMessage());

        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {

        log.warn("AuthenticationException [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());

        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed");
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("Validation error: {}", errorMessage);

        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {

        log.error("Unexpected exception: ", ex);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }


    private ResponseEntity<ApiResponse<Object>> buildResponse(HttpStatus status, String message) {

        ApiResponse<Object> response = ApiResponse.builder()
                .success(false)
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}