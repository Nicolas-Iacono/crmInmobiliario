package com.backend.crmInmobiliario.DTO.entrada.garante;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GaranteEntradaDto {

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

    private String cuit;

    @NotNull(message = "La dirección no puede ser nula")
    private String direccionResidencial;

    private String nacionalidad;

    private String estadoCivil;

    private String nombreEmpresa;

    private String sectorActual;

    private String cargoActual;

    private int legajo;

    private String cuitEmpresa;

    private String tipoGarantia;

    private String partidaInmobiliaria;
    private String direccion;
    private String infoCatastral;
    private String estadoOcupacion;
    private String tipoPropiedad;
    private String informeDominio;
    private String informeInhibicion;
    private String nombreUsuario;
}
