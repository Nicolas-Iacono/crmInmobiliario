package com.backend.crmInmobiliario.DTO.salida.propiedad;

import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.MonedaPropiedad;
import com.backend.crmInmobiliario.entity.TipoOperacionPropiedad;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PropiedadSalidaDtoNoRecursivo {
    private Long id;
    private String direccion;
    private String localidad;
    private String partido;
    private String provincia;
    private String tipo;
    private String inventario;
    private Boolean disponibilidad;
    private Integer cantidadAmbientes;
    private Boolean pileta;
    private Boolean cochera;
    private Boolean jardin;
    private Boolean patio;
    private Boolean balcon;
    private MonedaPropiedad moneda;
    private TipoOperacionPropiedad tipoOperacion;
    private boolean visibleAOtros;
    private List<ImgUrlSalidaDto> imagenes = new ArrayList<>();
    private Double metrosCuadradosCubierto;
    private Double metrosCuadradosDescubierto;
    private Double metrosCuadradosTotales;
    private Double metrosFrente;
    private Double metrosFondo;

}
