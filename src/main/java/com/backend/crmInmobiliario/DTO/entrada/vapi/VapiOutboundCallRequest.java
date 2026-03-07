package com.backend.crmInmobiliario.DTO.entrada.vapi;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VapiOutboundCallRequest {

    @NotBlank(message = "destinationNumber es obligatorio")
    private String destinationNumber;

    @NotBlank(message = "modo es obligatorio")
    private String modo;

    @NotBlank(message = "contexto es obligatorio")
    private String contexto;
}
