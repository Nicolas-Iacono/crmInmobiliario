package com.backend.crmInmobiliario.DTO.entrada.oficios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class OficioProveedorCreateDto {

    @NotBlank @Size(min = 3, max = 40)
    private String username;

    @NotBlank @Size(min = 6, max = 120)
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 120, message = "El nombre completo no puede superar 120 caracteres")
    private String nombreCompleto;

    @Size(max = 120, message = "La empresa no puede superar 120 caracteres")
    private String empresa;

    @Email(message = "Email inválido")
    @Size(max = 120, message = "El email no puede superar 120 caracteres")
    private String emailContacto;

    @Size(max = 40, message = "El teléfono no puede superar 40 caracteres")
    private String telefonoContacto;

    @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres")
    private String descripcion;

    @Size(max = 80, message = "La localidad no puede superar 80 caracteres")
    private String localidad;

    @Size(max = 80, message = "La provincia no puede superar 80 caracteres")
    private String provincia;

    @NotEmpty(message = "Debés elegir al menos una categoría")
    private List<@NotBlank(message = "Las categorías no pueden venir vacías") String> categorias;
}
