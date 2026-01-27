package com.backend.crmInmobiliario.DTO.salida.contrato;

import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.EstadoContrato;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class ContratoBasicoDto {
    private Long id;
    private String nombreContrato;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String direccionPropiedad;
    private String estado;
    private String contratoPdf;
    private int duracion;
    private String nombreInquilino;

    private String apellidoInquilino;
    private PropietarioContratoDtoSalida propietario;
    private InquilinoContratoDtoSalida inquilino;
    private UsuarioDtoSalida usuarioDtoSalida;
    private String tipoGarantia;
    private Set<EstadoContrato> estados;
}
