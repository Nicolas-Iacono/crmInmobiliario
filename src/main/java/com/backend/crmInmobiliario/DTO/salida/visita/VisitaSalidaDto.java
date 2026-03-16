package com.backend.crmInmobiliario.DTO.salida.visita;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VisitaSalidaDto {

    private Long id;
    private Long propiedadId;

    private String titulo;
    private LocalDate fecha;
    private LocalTime hora;
    private String aclaracion;
    private String nombreCorredor;

    private String visitanteNombre;
    private String visitanteApellido;
    private String visitanteTelefono;

    private Long prospectoId;
    private String prospectoNombreCompleto;

    public VisitaSalidaDto() {}

    // 👇 ESTE ES EL QUE TE FALTA (mismo orden que la query)
    public VisitaSalidaDto(
            Long id,
            Long propiedadId,
            String titulo,
            LocalDate fecha,
            LocalTime hora,
            String aclaracion,
            String nombreCorredor,
            String visitanteNombre,
            String visitanteApellido,
            String visitanteTelefono,
            Long prospectoId,
            String prospectoNombreCompleto
    ) {
        this.id = id;
        this.propiedadId = propiedadId;
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.aclaracion = aclaracion;
        this.nombreCorredor = nombreCorredor;
        this.visitanteNombre = visitanteNombre;
        this.visitanteApellido = visitanteApellido;
        this.visitanteTelefono = visitanteTelefono;
        this.prospectoId = prospectoId;
        this.prospectoNombreCompleto = prospectoNombreCompleto;
    }

    // getters/setters
}
