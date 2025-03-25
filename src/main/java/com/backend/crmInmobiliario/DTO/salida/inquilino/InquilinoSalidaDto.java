package com.backend.crmInmobiliario.DTO.salida.inquilino;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InquilinoSalidaDto {
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
    private UsuarioDtoSalida usuarioDtoSalida;

}
