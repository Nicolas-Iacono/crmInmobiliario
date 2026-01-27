package com.backend.crmInmobiliario.DTO.entrada;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantillaContratoDtoEntrada {

    private String nombre;
    private String descripcion;

    /** Contenido HTML con placeholders, ejemplo:
     *  "<p>Inquilino: {inquilino_nombre}</p>"
     */
    private String contenidoHtml;

    /** ID del usuario (inmobiliaria) propietario de la plantilla */
    private Long usuarioId;
}
