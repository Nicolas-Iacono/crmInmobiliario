package com.backend.crmInmobiliario.DTO.salida.garante;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GaranteUser {
    private String username;
    private String password;
}
