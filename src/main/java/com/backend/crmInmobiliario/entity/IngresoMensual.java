package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingresos_mensuales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngresoMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrato", nullable = false)
    private Contrato contrato;

    private int mes;
    private int anio;

    // 🔹 Valor del alquiler del contrato en ese mes
    @Column(name = "monto_alquiler", precision = 12, scale = 2)
    private BigDecimal montoAlquiler;

    // 🔹 Porcentaje de comisión que aplicás al ingreso mensual (por ejemplo, 10%)
    @Column(name = "porcentaje_comision_mensual", precision = 5, scale = 2)
    private BigDecimal porcentajeComisionMensual;

    // 🔹 Porcentaje de comisión general del contrato (por ejemplo, 5% del total)
    @Column(name = "porcentaje_comision_contrato", precision = 5, scale = 2)
    private BigDecimal porcentajeComisionContrato;

    // 🔹 Ingreso calculado por contrato (monto_alquiler * comision_contrato / 100)
    @Column(name = "ingreso_calculado_contrato", precision = 12, scale = 2)
    private BigDecimal ingresoCalculadoPorContrato;

    // 🔹 Ingreso calculado por mes (puede ser distinto si tenés ajuste o extra)
    @Column(name = "ingreso_calculado_mensual", precision = 12, scale = 2)
    private BigDecimal ingresoCalculadoPorMes;

    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}
