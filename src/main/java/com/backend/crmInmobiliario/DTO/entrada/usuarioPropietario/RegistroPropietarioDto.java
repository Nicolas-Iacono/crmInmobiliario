package com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario;

import lombok.Data;

@Data
public class RegistroPropietarioDto {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String dni;
}
