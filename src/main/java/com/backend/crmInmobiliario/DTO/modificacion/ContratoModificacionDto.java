package com.backend.crmInmobiliario.DTO.modificacion;

import com.backend.crmInmobiliario.entity.EstadoContrato;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class ContratoModificacionDto {

    private Long idContrato; // para saber cuál editar

    // ===== Datos básicos =====
    private String nombreContrato;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha_inicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha_fin;

    private Long id_propietario;   // si quisieras permitir cambiarlo
    private Long id_inquilino;     // idem
    private Long id_propiedad;     // idem

    private List<Long> garantesIds;

    // ===== Servicios =====
    private String aguaEmpresa;
    private BigDecimal aguaPorcentaje;
    private String aguaCuentaServicio;

    private String luzEmpresa;
    private BigDecimal luzPorcentaje;
    private String luzNroCliente;
    private String luzNroMedidor;

    private String gasEmpresa;
    private BigDecimal gasPorcentaje;
    private String gasNroCuenta;

    private String municipalEmpresa;
    private BigDecimal municipalPorcentaje;
    private String municipalNroCuenta;

    // ===== Condiciones contrato =====
    private Integer actualizacion;
    private Double  montoAlquiler;

    private String indiceAjuste;
    private String montoAlquilerLetras;
    private Double multaXDia;

    private Integer duracion;
    private String destino;

    private String tipoGarantia;
    private Set<EstadoContrato> estados;
    // ===== Comisiones =====
    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal comisionContratoPorc;

    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal comisionMensualPorc;

    // ===== Texto contrato (HTML/PDF) =====
    private String pdfContratoTexto;
}
