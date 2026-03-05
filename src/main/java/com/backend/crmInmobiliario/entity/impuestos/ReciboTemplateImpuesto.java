package com.backend.crmInmobiliario.entity.impuestos;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@Table(name="recibo_template_impuesto")
public class ReciboTemplateImpuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="template_id", nullable=false)
    private ReciboTemplate template;

    private String tipo;        // "AGUA" | "LUZ" | "GAS" | "MUNICIPAL" | "OTRO"
    private String empresa;
    private BigDecimal montoFijo;     // <-- clave para tu caso
    private BigDecimal porcentaje;    // opcional si algún día querés %
}
