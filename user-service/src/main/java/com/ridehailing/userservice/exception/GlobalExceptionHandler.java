package com.ridehailing.userservice.exception;

import com.ridehailing.userservice.common.ApiResponse;
import com.ridehailing.userservice.entity.DriverStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex) {

        log.warn("Handled BaseException [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());

        return buildResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {

        String error = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation error");

        log.warn("Validation error: {}", error);

        return buildResponse(HttpStatus.BAD_REQUEST, error);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleJsonParseError(HttpMessageNotReadableException ex) {

        log.warn("Invalid request body for DriverStatus: {}", ex.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        String message;

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {

            Object[] values = ex.getRequiredType().getEnumConstants();

            message = "Invalid value: " + ex.getValue()
                    + ". Allowed values: " + Arrays.toString(values);

        } else {
            message = "Invalid parameter: " + ex.getName();
        }

        log.warn("Type mismatch: {}", message);

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(EntityNotFoundException ex) {

        log.warn("Not found: {}", ex.getMessage());

        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {

        log.warn("Data integrity violation: {}", ex.getMessage());

        return buildResponse(HttpStatus.CONFLICT, "Database constraint violation");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex) {

        log.error("Runtime exception occurred", ex);

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {

        log.error("Unexpected exception occurred", ex);

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