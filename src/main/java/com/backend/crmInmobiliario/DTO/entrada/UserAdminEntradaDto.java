package com.backend.crmInmobiliario.DTO.entrada;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminEntradaDto {

    @NotNull(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String username;

    @NotBlank(message = "Debe tener una contraseña")
    private String password;

    @NotNull(message = "El nombre del negocio no puede ser nulo")
    @NotBlank(message = "Debe especificarse el nombre del negocio")
    @Size(max = 50, message = "El nombre del negocio puede tener hasta 50 caracteres")
    private String nombreNegocio;

    @Email(message = "el email debe ser valido")
    private String email;
}
