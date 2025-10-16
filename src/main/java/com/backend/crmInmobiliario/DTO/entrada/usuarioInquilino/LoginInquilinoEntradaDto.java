package com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino;

import lombok.Data;

@Data
public class LoginInquilinoEntradaDto {
    private String email;      // o username
    private String password;
}
