package com.backend.crmInmobiliario.DTO.salida.contrato;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ContratoBasicoDto {
    private Long id;
    private String nombreContrato;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String direccionPropiedad;
    private String estado;
    private String contratoPdf;
    private int duracion;
    private String nombreInquilino;
    private String apellidoInquilino;
}
