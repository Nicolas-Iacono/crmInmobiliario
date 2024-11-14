package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropiedadPropietarioSalidaDto {
    private Long id;
    private String nombre;
    private String apellido;
}
