package com.backend.crmInmobiliario.DTO.salida;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantillaContratoDtoSalida {

    private Long id;
    private String nombre;
    private String descripcion;

    /** HTML completo con placeholders */
    private String contenidoHtml;

    /** Datos básicos del usuario propietario (opcional) */
    private Long usuarioId;
    private String usuarioNombreNegocio;
}
