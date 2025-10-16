package com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InquilinoDtoSalida {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String cuit;
    private String email;
    private String telefono;
    private String direccion;
    private String localidad;
    private String provincia;

    // ID del usuario inmobiliaria (el que lo creó)
    private Long usuarioCreadorId;

    // ID del usuario inquilino (si tiene cuenta)
    private Long usuarioInquilinoId;

    // Info de contratos o resumen, opcional
    private int cantidadContratos;
}
