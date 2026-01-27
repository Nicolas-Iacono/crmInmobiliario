package com.backend.crmInmobiliario.DTO.salida.propietario;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropietarioUser {
    private String username;
    private String password;
}
