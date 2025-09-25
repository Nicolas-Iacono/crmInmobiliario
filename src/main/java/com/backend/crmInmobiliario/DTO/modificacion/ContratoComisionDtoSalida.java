package com.backend.crmInmobiliario.DTO.modificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratoComisionDtoSalida {

    private BigDecimal comisionContratoPorc;
    /** % comisión mensual */
    private BigDecimal comisionMensualPorc;

    /** monto = alquiler * meses * (comisionContratoPorc/100) */
    private BigDecimal comisionContratoMonto;
    /** monto = alquiler * (comisionMensualPorc/100) */
    private BigDecimal comisionMensualMonto;
    /** monto = alquiler - comisionMensualMonto */
    private BigDecimal montoMensualPropietario;
}
