package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ImpuestoMunicipalSalidaDto {
    private Long id;
    private String tipo;
    private String descripcion;
    private String empresa;
    private BigDecimal porcentaje;
    private String numeroCliente;
    private String numeroMedidor;
    private BigDecimal montoBase;
    private BigDecimal montoAPagar;
    private LocalDate fechaFactura;
    private Boolean estadoPago;
}
