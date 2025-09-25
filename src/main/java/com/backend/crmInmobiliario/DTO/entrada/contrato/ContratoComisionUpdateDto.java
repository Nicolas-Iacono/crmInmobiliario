package com.backend.crmInmobiliario.DTO.entrada.contrato;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ContratoComisionUpdateDto {
    private Long idContrato;
    private BigDecimal comisionContratoPorc; // 0..100 (nullable => no cambia)
    private BigDecimal comisionMensualPorc;  // 0..100 (nullable => no cambia)
}
