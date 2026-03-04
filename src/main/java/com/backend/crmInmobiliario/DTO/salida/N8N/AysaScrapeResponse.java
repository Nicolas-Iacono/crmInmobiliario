package com.backend.crmInmobiliario.DTO.salida.N8N;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class AysaScrapeResponse {

    private boolean ok;

    private Long contratoId;

    private String cuentaServicios;

    private String nroDocumento;

    private LocalDate fechaEmision;

    private LocalDate fechaVencimiento;

    private BigDecimal importe;

    private String pdfUrl;

    private String mensaje; // error o info
}
