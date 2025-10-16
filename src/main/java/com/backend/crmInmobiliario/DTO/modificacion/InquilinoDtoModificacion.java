package com.backend.crmInmobiliario.DTO.modificacion;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InquilinoDtoModificacion {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String cuit;
    private String email;
    private String telefono;
    private String direccionResidencial;
    private String localidad;
    private String provincia;
}
