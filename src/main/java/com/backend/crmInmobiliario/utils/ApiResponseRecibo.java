package com.backend.crmInmobiliario.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseRecibo<T> {
    private boolean success;
    private String message;
    private T data;

    // fábricas opcionales
    public static <T> ApiResponseRecibo<T> ok(String message, T data) {
        return new ApiResponseRecibo<>(true, message, data);
    }

}
