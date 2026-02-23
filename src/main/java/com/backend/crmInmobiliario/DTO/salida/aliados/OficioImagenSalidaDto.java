package com.backend.crmInmobiliario.DTO.salida.aliados;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OficioImagenSalidaDto {
    private Long idImage;
    private String imageUrl;
    private String nombreOriginal;
    private String tipoImagen;
}

