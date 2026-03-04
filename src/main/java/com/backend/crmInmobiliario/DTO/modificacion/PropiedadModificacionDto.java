package com.backend.crmInmobiliario.DTO.modificacion;

import com.backend.crmInmobiliario.entity.MonedaPropiedad;
import com.backend.crmInmobiliario.entity.TipoOperacionPropiedad;
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
    private MonedaPropiedad moneda;
    private TipoOperacionPropiedad tipoOperacion;
    private Integer cantidadAmbientes;
    private Boolean pileta;
    private Boolean cochera;
    private Boolean jardin;
    private Boolean patio;
    private Boolean balcon;
    private boolean visibleAOtros;
    private Double metrosCuadradosCubierto;
    private Double metrosCuadradosDescubierto;

    private Double metrosFrente;
    private Double metrosFondo;

    private Long propietarioId;
}
