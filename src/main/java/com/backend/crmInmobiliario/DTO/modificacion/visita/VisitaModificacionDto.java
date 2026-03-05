package com.backend.crmInmobiliario.DTO.modificacion.visita;

import lombok.Data;

@Data
public class VisitaModificacionDto {
    private String titulo;
    private String aclaracion;
    private String nombreCorredor;
    private Long prospectoId;
    private String visitanteNombre;
    private String visitanteApellido;
    private String visitanteTelefono;
}
