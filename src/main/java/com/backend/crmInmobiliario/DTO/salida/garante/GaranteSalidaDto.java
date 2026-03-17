package com.backend.crmInmobiliario.DTO.salida.garante;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoIdSalidaDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GaranteSalidaDto {

    private Long id;
    private ContratoIdSalidaDto contrato;
    private String pronombre;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String dni;
    private String cuit;
    private String direccionResidencial;
    private String nacionalidad;
    private String estadoCivil;

    private String nombreEmpresa;
    private String sectorActual;
    private String cargoActual;
    private Long legajo;
    private String cuitEmpresa;

    private String tipoGarantia;
    private String partidaInmobiliaria;
    private String direccion;
    private String infoCatastral;
    private String estadoOcupacion;
    private String tipoPropiedad;
    private String informeDominio;
    private String informeInhibicion;

    private Long usuarioCuentaId;
    private GaranteUser usuarioCuentaGarante;
    private UsuarioDtoSalida usuarioDtoSalida;
}
