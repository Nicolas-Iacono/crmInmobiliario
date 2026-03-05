package com.backend.crmInmobiliario.DTO.entrada.contrato;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContratoImpuestoTemplateDto {
    private Long id;
    private String tipoImpuesto;
    private String descripcion;
    private String empresa;
    private String numeroCliente;
    private String numeroMedidor;
    private BigDecimal montoBase;
    private BigDecimal porcentaje;
    private Boolean activo;
}
