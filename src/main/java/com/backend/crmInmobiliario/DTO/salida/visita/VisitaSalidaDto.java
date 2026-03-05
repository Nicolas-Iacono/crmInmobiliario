package com.backend.crmInmobiliario.DTO.salida.visita;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VisitaSalidaDto {
    private Long id;
    private Long propiedadId;
    private Long prospectoId;
    private String prospectoNombreCompleto;
    private String titulo;
    private LocalDate fecha;
    private LocalTime hora;
    private String aclaracion;
    private String nombreCorredor;
    private String visitanteNombre;
    private String visitanteApellido;
    private String visitanteTelefono;
}
