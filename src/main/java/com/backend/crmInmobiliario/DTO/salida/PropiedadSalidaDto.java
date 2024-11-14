package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropiedadSalidaDto {
    private Long id;
    private String direccion;
    private String localidad;
    private String partido;
    private String provincia;
    private String tipo;
    private String inventario;
    private Boolean disponibilidad;
    private PropietarioContratoDtoSalida propietarioSalidaDto;
    private UsuarioDtoSalida usuarioDtoSalida;
}
