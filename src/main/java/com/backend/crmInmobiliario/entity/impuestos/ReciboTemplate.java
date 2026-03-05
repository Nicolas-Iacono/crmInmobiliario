package com.backend.crmInmobiliario.entity.impuestos;

import com.backend.crmInmobiliario.entity.Contrato;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@Table(name="recibo_template")
public class ReciboTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="contrato_id", nullable=false, unique=true)
    private Contrato contrato;

    private String conceptoDefault;        // ej: "Alquiler"
    private BigDecimal montoBase;          // monto fijo (si aplica)
    private boolean usarMontoContrato;     // si true: toma contrato.montoAlquiler
}
