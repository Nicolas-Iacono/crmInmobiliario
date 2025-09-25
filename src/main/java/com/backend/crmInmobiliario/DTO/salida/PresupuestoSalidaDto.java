package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PresupuestoSalidaDto {
    // mismos campos base
    private Long id;
    private Long usuarioId;
    private String titulo;
    private Double monto;
    private String porcentajeContrato;
    private String porcentajeSello;
    private int duracion;
    private Double gastosExtras;

    // campos calculados
    private BigDecimal primerMes;   // = monto
    private BigDecimal deposito;    // = monto
    private BigDecimal sellado;     // = monto * duracion * %sello
    private BigDecimal honorarios;  // = monto * duracion * %contrato
    private BigDecimal total;       // = primerMes + sellado + honorarios + gastosExtras + deposito
}
