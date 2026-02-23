package com.backend.crmInmobiliario.DTO.entrada.aliados;

import lombok.Data;

@Data
public class OficioCalificacionEntradaDto {
    private Integer estrellas; // 1..5
    private String comentario;
}

