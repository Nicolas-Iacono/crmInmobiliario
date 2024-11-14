package com.backend.crmInmobiliario.DTO;

import com.backend.crmInmobiliario.DTO.salida.TokenDtoSalida;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"username","message","jwt","status"})
public record AuthResponse(
        String username,
        String message,
        String jwt,
        boolean status


) {
}
