package com.backend.crmInmobiliario.DTO.entrada.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RenovarContratoRequest(
        LocalDate fechaInicio,
        LocalDate fechaFin,              // si viene null => fecha_fin viejo + 1
        Integer duracion,                   // si viene null => copia del viejo
        Double montoAlquiler,
        Integer actualizacion,
        String indiceAjuste,
        String montoAlquilerLetras,
        Double multaXDia,
        String destino,
        String tipoGarantia,

        List<Long> garantesIds,             // null => clonar garantes viejos, [] => sin garantes

        String aguaEmpresa,
        BigDecimal aguaPorcentaje,
        String luzEmpresa,
        BigDecimal luzPorcentaje,
        String gasEmpresa,
        BigDecimal gasPorcentaje,
        String municipalEmpresa,
        BigDecimal municipalPorcentaje,

        BigDecimal comisionContratoPorc,
            BigDecimal comisionMensualPorc
) {}
