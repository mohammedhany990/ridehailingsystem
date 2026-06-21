package com.ridehailing.locationservice.exception;

import com.ridehailing.locationservice.common.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex) {

        log.warn("Handled exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());

        return buildResponse(ex.getStatus(), ex.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadArg(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
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
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
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
