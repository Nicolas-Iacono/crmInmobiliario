package com.backend.crmInmobiliario.DTO.mpDtos.transferencias.entrada;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class NotificarTransferenciaDto {

    @NotNull(message = "El monto es obligatorio")
    private BigDecimal amount;

    private String reference; // opcional
    private String comprobanteUrl; // opcional (si lo subís a storage)
    private String note; // opcional
}
