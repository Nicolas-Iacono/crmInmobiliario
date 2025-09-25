package com.backend.crmInmobiliario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ContractLimitExceededException.class)
    public ResponseEntity<Map<String,Object>> handleLimit(ContractLimitExceededException ex) {
        Map<String,Object> body = new HashMap<>();
        body.put("error", "CONTRACT_LIMIT_EXCEEDED");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
