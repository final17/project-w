package com.projectw.common.exceptions;

import com.projectw.common.dto.ErrorResponse;
import com.projectw.security.AuthUser;
import com.projectw.security.JwtAuthenticationToken;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException apiException) {
        ErrorResponse errorResponse = ErrorResponse.from(apiException);
        logException(apiException.getHttpStatus() , errorResponse.getMessage());
        return new ResponseEntity<>(errorResponse, apiException.getHttpStatus());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleException(Exception exception) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse errorResponse = ErrorResponse.of(httpStatus.value(), exception.getMessage());
        logException(httpStatus , errorResponse.getMessage());
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

    private void logException(HttpStatus httpStatus, String message) {
        AuthUser auth = null;
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        auth = authentication == null ? null : (AuthUser) authentication.getPrincipal();

        if (auth != null) {
            MDC.put("email", auth.getEmail());
        }
        MDC.put("status", String.valueOf(httpStatus.value()));
        MDC.put("message", message);
        MDC.put("type", "Error");

        log.error("Error : {} , Message : {}", httpStatus, message);

        // MDC에서 값을 제거하여 메모리 누수를 방지합니다.
        MDC.clear();
    }
}
