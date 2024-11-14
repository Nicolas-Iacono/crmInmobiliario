package com.backend.crmInmobiliario.DTO.entrada;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InquilinoEntradaDto {

    private String pronombre;

    @NotNull(message = "El nombre no puede ser nulo")
    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres")
    private String nombre;

    @NotNull(message = "El apellido no puede ser nulo")
    @Size(min = 2, message = "El apellido debe tener al menos 2 caracteres")
    private String apellido;

    @NotNull(message = "El teléfono no puede ser nulo")
    @Pattern(regexp = "\\d{10,15}", message = "El teléfono debe tener entre 10 y 15 dígitos")
    private String telefono;

    @Email(message = "Debe ser una dirección de correo válida")
    private String email;

    @NotNull(message = "El DNI no puede ser nulo")
    @Pattern(regexp = "\\d{7,8}", message = "El DNI debe ser un número válido de 7 u 8 dígitos")
    private String dni;

    @NotNull(message = "La dirección no puede ser nula")
    private String direccionResidencial;

    private String cuit;

    private String nacionalidad;

    private String estadoCivil;

    private String nombreUsuario;

}
