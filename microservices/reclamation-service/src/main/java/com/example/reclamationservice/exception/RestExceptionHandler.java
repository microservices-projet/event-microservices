package com.example.reclamationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(RestExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Donnees invalides";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

    private static String formatFieldError(FieldError e) {
        return e.getField() + ": " + (e.getDefaultMessage() != null ? e.getDefaultMessage() : "invalide");
    }
}
