package com.backend.crmInmobiliario.repository.projections;

import java.math.BigDecimal;

public interface ReciboSyncProjection {
    Long getIdRecibo();
    Long getContratoId();
    Long getUserId();

    String getNombreContrato();

    String getNumeroRecibo();
    BigDecimal getMontoTotal();
    String getPeriodo();
    String getConcepto();

    String getFechaEmision();      // ya como String ISO (yyyy-MM-dd) o null
    String getFechaVencimiento();  // ya como String ISO (yyyy-MM-dd) o null

    Boolean getEstado(); // pagado
}
