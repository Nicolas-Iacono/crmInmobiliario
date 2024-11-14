package com.backend.crmInmobiliario.DTO.salida.propiedad;

import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import lombok.Data;
import lombok.NoArgsConstructor;

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

}
