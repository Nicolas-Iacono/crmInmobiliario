package com.backend.crmInmobiliario.DTO.salida.planesYSuscripcion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSalidaDto {
    private Long id;
    private String planName;
    private String mpPaymentId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime paymentDate;
}
