package com.backend.crmInmobiliario.DTO.salida;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngresoMensualResumenDto {

    private int mes; // Ejemplo: 1 = enero, 2 = febrero...
    private int anio; // Año del registro (ej. 2025)

    private BigDecimal totalComisionesMensuales; // Suma de ingresoCalculadoPorMes
    private BigDecimal totalHonorariosContrato;  // Suma de ingresoCalculadoPorContrato

    public BigDecimal getTotalDelMes() {
        BigDecimal mensual = totalComisionesMensuales != null ? totalComisionesMensuales : BigDecimal.ZERO;
        BigDecimal contrato = totalHonorariosContrato != null ? totalHonorariosContrato : BigDecimal.ZERO;
        return mensual.add(contrato);
    }
}
