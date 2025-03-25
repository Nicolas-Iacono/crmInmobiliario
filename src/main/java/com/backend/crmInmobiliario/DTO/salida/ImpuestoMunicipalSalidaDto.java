package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ImpuestoMunicipalSalidaDto {
    private Long id;
    private String tipo;
    private String descripcion;
    private String empresa;
    private int porcentaje;
    private String numeroCliente;
    private String numeroMedidor;
    private Double montoAPagar;
    private LocalDate fechaFactura;
    private Boolean estadoPago;
}
