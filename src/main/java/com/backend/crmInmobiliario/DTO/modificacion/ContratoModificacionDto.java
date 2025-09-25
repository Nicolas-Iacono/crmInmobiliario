package com.backend.crmInmobiliario.DTO.modificacion;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ContratoModificacionDto {
    private Long idContrato;
    private String pdfContratoTexto;
    private Double montoAlquiler;
    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal comisionContratoPorc;

    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal comisionMensualPorc;
}
