package com.backend.crmInmobiliario.DTO.entrada.propiedades;


import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propietario;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropiedadEntradaDto {

    @NotNull(message = "La dirección no puede ser nula")
    private String direccion;

    @NotNull(message = "La localidad no puede ser nula")
    private String localidad;

    @NotNull(message = "El partido no puede ser nulo")
    private String partido;

    @NotNull(message = "La provincia no puede ser nula")
    private String provincia;

    private Boolean disponibilidad;

    private Long id_propietario;

    private String inventario;
    private String tipo;

    private String nombreUsuario;
}
