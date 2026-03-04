package com.backend.crmInmobiliario.entity.impuestos;

import com.backend.crmInmobiliario.entity.Contrato;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "contrato_impuesto_template",
        indexes = {@Index(name="idx_tpl_contrato", columnList="contrato_id")}
)
@Data
@NoArgsConstructor
public class ContratoImpuestoTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="contrato_id", nullable=false)
    private Contrato contrato;

    @Column(nullable=false)
    private String tipoImpuesto; // "AGUA", "GAS", ...

    private String descripcion;
    private String empresa;
    private String numeroCliente;
    private String numeroMedidor;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal montoBase;     // 👈 base del impuesto

    @Column(precision=6, scale=2)
    private BigDecimal porcentaje;    // 👈 si aplica (sobre montoBase)

    @Column(nullable=false)
    private boolean activo = true;

    // getters/setters...
}
