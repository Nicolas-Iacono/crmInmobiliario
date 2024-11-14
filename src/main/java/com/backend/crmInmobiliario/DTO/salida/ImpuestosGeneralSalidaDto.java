package com.backend.crmInmobiliario.DTO.salida;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImpuestosGeneralSalidaDto {

    private ImpuestoAguaSalidaDto impuestoAguaSalidaDto;

    private ImpuestoGasSalidaDto impuestoGasSalidaDto;

    private ImpuestoLuzSalidaDto impuestoLuzSalidaDto;

    private ImpuestoMunicipalSalidaDto impuestoMunicipalSalidaDto;

}
