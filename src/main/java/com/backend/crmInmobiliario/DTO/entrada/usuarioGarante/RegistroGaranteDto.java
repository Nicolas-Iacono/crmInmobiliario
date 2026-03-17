package com.backend.crmInmobiliario.DTO.entrada.usuarioGarante;

import lombok.Data;

@Data
public class RegistroGaranteDto {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String dni;
}
