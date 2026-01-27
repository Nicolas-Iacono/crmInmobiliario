package com.backend.crmInmobiliario.DTO.entrada.contrato;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ContratoEntradaDto {

    @NotNull(message = "El contrato debe tener un nombre")
    private String nombreContrato;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha_inicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha_fin;

    @NotNull(message = "debe asignarse un propietario")
    private Long id_propietario;

    @NotNull(message = "debe asignarse un inquilino")
    private Long id_inquilino;

    @NotNull(message = "debe asignarse una propiedad")
    private Long id_propiedad;

    private List<Long> garantesIds;

    private String aguaEmpresa;
    private BigDecimal aguaPorcentaje;

    private String luzEmpresa;
    private BigDecimal luzPorcentaje;

    private String gasEmpresa;
    private BigDecimal gasPorcentaje;

    private String municipalEmpresa;
    private BigDecimal municipalPorcentaje;

    private int actualizacion;
    private Double montoAlquiler;

    private String indiceAjuste;
    private String montoAlquilerLetras;
    private Double multaXDia;

    private int duracion;
    private String destino;

    private String tipoGarantia;

    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal comisionContratoPorc;

    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal comisionMensualPorc;
}
