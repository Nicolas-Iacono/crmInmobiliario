package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ImgUrlSalidaDto {
    private Long idImage;
    private String imageUrl;
    private String nombreOriginal;
    private String tipoImagen;
    private LocalDateTime fechaSubida;

}
