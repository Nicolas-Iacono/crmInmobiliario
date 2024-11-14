package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImgUrlSalidaDto {
    private Long idImage;
    private Long garante_id;
    private String imageUrl;

}
