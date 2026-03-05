package com.backend.crmInmobiliario.DTO.entrada.visita;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VisitaEntradaDto {
    @NotNull
    private Long propiedadId;

    private Long prospectoId;

    @NotBlank
    private String titulo;

    @NotNull
    private LocalDate fecha;

    @NotNull
    private LocalTime hora;

    private String aclaracion;

    @NotBlank
    private String nombreCorredor;

    private String visitanteNombre;
    private String visitanteApellido;
    private String visitanteTelefono;
}
