package com.backend.crmInmobiliario.DTO.salida;

import com.backend.crmInmobiliario.entity.MonedaPropiedad;
import com.backend.crmInmobiliario.entity.TipoOperacionPropiedad;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PropiedadContratoSalidaDto {
    private Long id;
    private String direccion;
    private String localidad;
    private String partido;
    private String provincia;
    private String inventario;
    private String tipo;
    private Integer cantidadAmbientes;
    private Boolean pileta;
    private Boolean cochera;
    private Boolean jardin;
    private Boolean patio;
    private Boolean balcon;
    private Double precio;
    private MonedaPropiedad moneda;
    private TipoOperacionPropiedad tipoOperacion;
    private boolean visibleAOtros;
    private boolean propia;

    private List<ImgUrlSalidaDto> imagenes = new ArrayList<>();
}
