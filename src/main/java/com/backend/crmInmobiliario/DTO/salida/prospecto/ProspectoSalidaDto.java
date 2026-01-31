package com.backend.crmInmobiliario.DTO.salida.prospecto;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProspectoSalidaDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private BigDecimal rangoPrecioMin;
    private BigDecimal rangoPrecioMax;
    private Integer cantidadPersonas;
    private List<String> zonaPreferencia;
    private Integer cantidadAmbientes;
    private Boolean cochera;
    private Boolean patio;
    private Boolean jardin;
    private Boolean pileta;
    private Boolean visibilidadPublico;
    private Boolean destino;
    private Boolean mascotas;
    private Boolean disponible;
    private String nombreNegocio;
    private String logo;
    private String telefonoUsuario;
}
