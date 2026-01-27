package com.backend.crmInmobiliario.DTO.salida;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IngresoMensualSalidaDto {
    private Long id;
    private int mes;
    private int anio;
    private BigDecimal montoAlquiler;
    private BigDecimal porcentajeComisionContrato;
    private BigDecimal porcentajeComisionMensual;
    private BigDecimal ingresoCalculadoPorContrato;
    private BigDecimal ingresoCalculadoPorMes;
    private String nombreUsuario;
    private Long userId;
    private String nombreContrato;
}
