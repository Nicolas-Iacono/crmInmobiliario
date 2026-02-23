package com.backend.crmInmobiliario.DTO.salida.aliados;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResenaSalidaDto {
    private Long id;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime fechaCreacion;

    // info mínima del autor
    private Long usuarioId;
    private String username;
    private String nombreNegocio;
}
