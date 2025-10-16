package com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino;

import lombok.Data;

@Data
public class RegistroInquilinoDto {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String dni;
}
