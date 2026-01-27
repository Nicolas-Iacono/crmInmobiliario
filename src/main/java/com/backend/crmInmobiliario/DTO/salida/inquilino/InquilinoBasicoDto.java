package com.backend.crmInmobiliario.DTO.salida.inquilino;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InquilinoBasicoDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
}
