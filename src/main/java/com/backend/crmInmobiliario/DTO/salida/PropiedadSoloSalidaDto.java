package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PropiedadSoloSalidaDto {
    private Long id;
    private String direccion;
    private String localidad;
    private String partido;
    private String provincia;
    private Boolean disponibilidad;
    private Integer cantidadAmbientes;
    private Boolean pileta;
    private Boolean cochera;
    private Boolean jardin;
    private Boolean patio;
    private PropietarioContratoDtoSalida propietarioContratoDtoSalida;
    private List<ImgUrlSalidaDto> imagenes = new ArrayList<>();
    private UsuarioDtoSalida usuarioDtoSalida;

}
