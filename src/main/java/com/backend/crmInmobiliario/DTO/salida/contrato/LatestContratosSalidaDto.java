package com.backend.crmInmobiliario.DTO.salida.contrato;

import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.*;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class LatestContratosSalidaDto {
    private Long id;
    private String nombreContrato;
    private PropietarioContratoDtoSalida propietario;
    private InquilinoContratoDtoSalida inquilino;
    private PropiedadContratoSalidaDto propiedad;
}
