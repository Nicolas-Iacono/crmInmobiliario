package com.backend.crmInmobiliario.DTO.modificacion;

import lombok.Data;

@Data
public class PropiedadModificacionDto {

    private String direccion;
    private String localidad;
    private String partido;
    private String provincia;
    private String tipo;
    private String inventario;
    private Boolean disponibilidad;
    private Double precio;
    private Integer cantidadAmbientes;
    private Boolean pileta;
    private Boolean cochera;
    private Boolean jardin;
    private Boolean patio;
    private boolean visibleAOtros;

    private Long propietarioId;
}
