package com.backend.crmInmobiliario.DTO.entrada.renovaciones;

import com.backend.crmInmobiliario.DTO.entrada.garante.GaranteEntradaDto;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RenovarContratoRequest {
    LocalDate fechaInicio;
    LocalDate fechaFin;
    Integer duracion;
    Double montoAlquiler;
    Integer actualizacion;
    String indiceAjuste;
    String montoAlquilerLetras;
    Double multaXDia;
    String destino;
    String tipoGarantia;

    List<Long> garantesIds;

    String aguaEmpresa;
    BigDecimal aguaPorcentaje;
    String luzEmpresa;
    BigDecimal luzPorcentaje;
    String gasEmpresa;
    BigDecimal gasPorcentaje;
    String municipalEmpresa;
    BigDecimal municipalPorcentaje;

    BigDecimal comisionContratoPorc;
    BigDecimal comisionMensualPor;
}
