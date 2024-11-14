package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ReciboSalidaDto {
    private Long id;            // ID del recibo generado
    private LocalDate fechaEmision;   // Fecha en que se emitió el recibo
    private LocalDate periodo;        // Periodo del recibo
    private BigDecimal montoTotal;    // Monto total del recibo

    // Información del contrato asociado
    private String propietarioNombre;  // Nombre completo del propietario
    private String inquilinoNombre;    // Nombre completo del inquilino
    private String propiedadDireccion; // Dirección de la propiedad alquilada

    // Detalles del contrato
    private Double montoAlquiler;      // Monto del alquiler

    // Información de los impuestos o servicios
    private String empresaGas;         // Nombre de la empresa de gas
    private Double porcentajeGas;      // Porcentaje de impuesto del gas
    private Double montoGasMensual;    // Monto mensual del gas

    private String empresaLuz;         // Nombre de la empresa de luz
    private Double porcentajeLuz;      // Porcentaje de impuesto de la luz
    private Double montoLuzMensual;    // Monto mensual de la luz

    private String empresaMuni;        // Nombre de la empresa municipal
    private Double porcentajeMuni;     // Porcentaje del impuesto municipal
    private Double montoMuniMensual;   // Monto mensual del impuesto municipal

    private String empresaAgua;        // Nombre de la empresa de agua
    private Double porcentajeAgua;     // Porcentaje del servicio de agua
    private Double montoAguaMensual;   // Monto mensual del agua


}
