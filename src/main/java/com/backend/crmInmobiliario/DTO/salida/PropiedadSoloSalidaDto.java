package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropiedadSoloSalidaDto {
    private Long id;
    private String direccion;
    private String localidad;
    private String partido;
    private String provincia;
    private Boolean disponibilidad;
    private PropietarioContratoDtoSalida propietarioContratoDtoSalida;
    private UsuarioDtoSalida usuarioDtoSalida;
}
