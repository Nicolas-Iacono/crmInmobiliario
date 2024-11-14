package com.backend.crmInmobiliario.DTO.entrada.inquilino;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InquilinoContratoDtoSalida {
    private Long id;
    private String pronombre;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String dni;
    private String direccionResidencial;
    private String cuit;
    private String nacionalidad;
    private String estadoCivil;
}
