package com.backend.crmInmobiliario.DTO.modificacion;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ActualizarUsuarioDto {
    private String nombreNegocio;
    @Email
    private String email;
    private String matricula;
    private String razonSocial;
    private String localidad;
    private String partido;
    private String provincia;
    private String cuit;
    private String telefono;
}
