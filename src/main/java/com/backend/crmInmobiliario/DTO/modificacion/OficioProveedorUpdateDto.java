package com.backend.crmInmobiliario.DTO.modificacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OficioProveedorUpdateDto {
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

    // Si viene null -> no tocar. Si viene [] -> vaciar categorías.
    private List<@Size(min = 1, max = 120, message = "Categoría inválida") String> categorias;

}
