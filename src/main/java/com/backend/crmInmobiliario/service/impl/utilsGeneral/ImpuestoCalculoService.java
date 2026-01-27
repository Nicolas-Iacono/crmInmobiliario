package com.backend.crmInmobiliario.service.impl.utilsGeneral;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ImpuestoCalculoService {

    public BigDecimal calcularMontoPorcentaje(
            BigDecimal base,
            BigDecimal porcentaje) {

        if (base == null || porcentaje == null) {
            return BigDecimal.ZERO;
        }

        return base
                .multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
