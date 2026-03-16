package com.backend.crmInmobiliario.DTO.entrada.usuarioGarante;

import lombok.Data;

@Data
public class LoginGaranteEntradaDto {
    private String email;      // o username
    private String password;
}
