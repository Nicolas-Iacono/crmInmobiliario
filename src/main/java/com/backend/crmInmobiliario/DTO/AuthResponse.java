package com.backend.crmInmobiliario.DTO;

import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"username","message","jwt","refreshToken","tokenType","expiresInSeconds","status"})
public record AuthResponse(


        String username,
        String message,
        String jwt,
        String refreshToken,
        String tokenType,
        int expiresInSeconds,
        boolean status

) {
}
