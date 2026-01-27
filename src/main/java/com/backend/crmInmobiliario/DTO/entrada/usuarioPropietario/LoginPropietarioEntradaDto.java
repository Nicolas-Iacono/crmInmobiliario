package com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario;

import lombok.Data;

@Data
public class LoginPropietarioEntradaDto {
    private String email;      // o username
    private String password;
}
