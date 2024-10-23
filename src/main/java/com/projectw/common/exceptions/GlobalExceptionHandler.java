package com.projectw.common.exceptions;

import com.projectw.common.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException apiException) {
        ErrorResponse errorResponse = ErrorResponse.from(apiException);
        return new ResponseEntity<>(errorResponse, apiException.getHttpStatus());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleException(Exception exception) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.of(httpStatus.value(), exception.getMessage());
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(c -> errors.put(((FieldError) c).getField(), c.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(errors);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    ResponseEntity<Map<String, String>> onConstraintValidationException(
            ConstraintViolationException e) {
        var constraintViolations = e.getConstraintViolations();
        Map<String, String> errors = new HashMap<>();
        for (final var constraint : constraintViolations) {

            String message = constraint.getMessage();
            String[] split = constraint.getPropertyPath()
                    .toString()
                    .split("\\.");
            String propertyPath = split[split.length - 1];
            errors.put(propertyPath, message);

        }
        return ResponseEntity.badRequest()
                .body(errors);
    }
}
