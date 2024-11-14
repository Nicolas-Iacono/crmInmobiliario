package com.backend.crmInmobiliario.DTO.salida.garante;

import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
public class GaranteContratoDtoSalida {
    private Long id;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private PropietarioContratoDtoSalida propietario;
    private InquilinoSalidaDto inquilino;
    private PropiedadContratoSalidaDto propiedad;
    private int actualizacion;
    private double montoAlquiler;
    private int duracion;
    private boolean activo;
}
