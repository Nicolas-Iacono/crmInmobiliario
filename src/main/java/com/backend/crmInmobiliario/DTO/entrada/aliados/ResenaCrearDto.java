package com.backend.crmInmobiliario.DTO.entrada.aliados;

import lombok.Data;

@Data
public class ResenaCrearDto {
    private Integer calificacion; // 1..5
    private String comentario;    // opcional
}