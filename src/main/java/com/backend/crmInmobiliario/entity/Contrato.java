package com.backend.crmInmobiliario.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Table(name = "contrato")
@Entity
@NoArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Long id;
    private String nombreContrato;
    @Column(name = "fecha_inicio", columnDefinition = "DATE",updatable = false,       // ⬅️ clave
            nullable = false)
    private LocalDate fecha_inicio;

    @Column(name = "fecha_fin", columnDefinition = "DATE",updatable = false,       // ⬅️ clave
            nullable = false)
    private LocalDate fecha_fin;

    @ManyToOne
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "usuario_id")  // Ajusta el nombre de la columna según tu esquema de base de datos
    private Usuario usuario;

    @CreationTimestamp
    @Column(
            name = "public_date",
            columnDefinition = "DATE",
            updatable = false,       // ⬅️ clave
            nullable = false
    )
    private LocalDate publicDate;

@Lob
@Column(name = "inventario", length = 20000)
    private String pdfContratoTexto;

    @ManyToOne
    @JoinColumn(name = "id_propietario", nullable = false)
    private Propietario propietario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inquilino", nullable = false)
    private Inquilino inquilino;

    @ManyToOne
    @JoinColumn(name = "id_propiedad", nullable = false)
    private Propiedad propiedad;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    private List<Garante> garantes;


    private String aguaEmpresa;
    @Column(precision = 5, scale = 2)
    private BigDecimal aguaPorcentaje;

    private String luzEmpresa;
    @Column(precision = 5, scale = 2)
    private BigDecimal luzPorcentaje;

    private String gasEmpresa;
    @Column(precision = 5, scale = 2)
    private BigDecimal gasPorcentaje;

    private String municipalEmpresa;
    @Column(precision = 5, scale = 2)
    private BigDecimal municipalPorcentaje;


    @Column(name = "tipo_garantia")
    private String tipoGarantia;

    private int actualizacion;
    @Column(name = "monto_alquiler", nullable = false)
    private Double montoAlquiler;
    private String montoAlquilerLetras;
    private Double multaXDia;
    private boolean activo;
    private String indiceAjuste;
    private int duracion;
    private Long tiempoRestante;
    private String destino;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY)
    private List<Recibo> recibos;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY)
    private List<Nota> notas;

    @Column(name = "comision_contrato_porc", precision = 5, scale = 2, nullable = false)
    private BigDecimal comisionContratoPorc = BigDecimal.ZERO;

    @Column(name = "comision_mensual_porc", precision = 5, scale = 2, nullable = false)
    private BigDecimal comisionMensualPorc = BigDecimal.ZERO;

    @Column(name = "suscrito", nullable = false)
    private boolean suscrito = true; // cuenta sólo si está activo

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documento> documentos = new ArrayList<>();

    public boolean isActivo() {
        return activo;
    }


    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoContrato estado = EstadoContrato.INACTIVO;

    @Transient
    public BigDecimal getComisionContratoMonto() {
        if (montoAlquiler == null || duracion <= 0) return BigDecimal.ZERO;
        BigDecimal alquiler = BigDecimal.valueOf(montoAlquiler);
        BigDecimal base = alquiler.multiply(BigDecimal.valueOf(duracion));
        return base.multiply(nullSafePercent(comisionContratoPorc));
    }


    /** Monto de comisión mensual (sobre montoAlquiler) */
    @Transient
    public BigDecimal getComisionMensualMonto() {
        if (montoAlquiler == null) return BigDecimal.ZERO;
        BigDecimal alquiler = BigDecimal.valueOf(montoAlquiler);
        return alquiler.multiply(nullSafePercent(comisionMensualPorc));
    }

    /** Monto a liquidar al propietario cada mes = alquiler - comisión mensual */
    @Transient
    public BigDecimal getLiquidacionPropietarioMensual() {
        if (montoAlquiler == null) return BigDecimal.ZERO;
        BigDecimal alquiler = BigDecimal.valueOf(montoAlquiler);
        return alquiler.subtract(getComisionMensualMonto());
    }

    private BigDecimal nullSafePercent(BigDecimal p) {
        BigDecimal v = (p == null ? BigDecimal.ZERO : p);
        // pasa de 3.5 a 0.035
        return v.divide(BigDecimal.valueOf(100));
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "contrato_estados",
            joinColumns = @JoinColumn(name = "contrato_id")
    )
    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private Set<EstadoContrato> estados = new HashSet<>();

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "contrato_anterior_id")
//    private Contrato contratoAnterior;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "contrato_renovado_id")
//    private Contrato contratoRenovado;
//
//    @Column(name = "fecha_renovacion")
//    private LocalDate fechaRenovacion;
}
