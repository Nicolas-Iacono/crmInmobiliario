package com.backend.crmInmobiliario.utils;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class FieldValidationException extends RuntimeException {

    private final Map<String, String> errors = new HashMap<>();

    public FieldValidationException() {
        super("Errores de validación");
    }

    public void addError(String field, String message) {
        errors.put(field, message);
    }
}
