package com.backend.crmInmobiliario.DTO.salida.contrato;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContratoComisionDtoSalida {

    private BigDecimal comisionContratoPorc;
    private BigDecimal comisionMensualPorc;
    private BigDecimal comisionContratoMonto;     // alquiler * meses * (%/100)  (calculado @Transient)
    private BigDecimal comisionMensualMonto;      // alquiler * (%/100)          (calculado @Transient)
    private BigDecimal montoMensualPropietario;   // alquiler - comisión mensual (calculado @Transient)

}
