package com.backend.crmInmobiliario.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
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
    private Long id_contrato;
    private String nombreContrato;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;

    @ManyToOne
    @ToString.Exclude
    @JsonIgnore
    @JoinColumn(name = "usuario_id")  // Ajusta el nombre de la columna según tu esquema de base de datos
    private Usuario usuario;

    @CreationTimestamp
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date publicDate;
//    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JoinColumn(name = "pdf_contrato_id")  // Columna en la tabla 'contrato' que hace referencia a la clave primaria de 'PdfContrato'
//    private PdfContrato pdfContrato;
@Lob
@Column(name = "inventario", length = 20000)
    private String pdfContratoTexto;

    @ManyToOne
    @JoinColumn(name = "id_propietario", nullable = false)
    private Propietario propietario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_inquilino", nullable = false)
    private Inquilino inquilino;

    @ManyToOne
    @JoinColumn(name = "id_propiedad", nullable = false)
    private Propiedad propiedad;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY,cascade = CascadeType.PERSIST)
    private List<Garante> garantes;


    private String aguaEmpresa;
    private int aguaPorcentaje;

    private String luzEmpresa;
    private int luzPorcentaje;

    private String gasEmpresa;
    private int gasPorcentaje;

    private String municipalEmpresa;
    private int municipalPorcentaje;




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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

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
}
