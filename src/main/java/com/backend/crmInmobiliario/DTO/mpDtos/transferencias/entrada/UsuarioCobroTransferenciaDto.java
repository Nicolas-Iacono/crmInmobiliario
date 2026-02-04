package com.backend.crmInmobiliario.DTO.mpDtos.transferencias.entrada;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioCobroTransferenciaDto {

    @NotBlank(message = "El alias es obligatorio")
    private String alias;

    private String cbu; // opcional si usa solo alias

    @NotBlank(message = "El titular es obligatorio")
    private String titular;

    @NotBlank(message = "El CUIT es obligatorio")
    private String cuit;

    private String banco;
}
