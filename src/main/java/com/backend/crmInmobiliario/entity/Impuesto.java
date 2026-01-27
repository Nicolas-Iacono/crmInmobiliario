package com.backend.crmInmobiliario.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)  // o SINGLE_TABLE, según convenga
@NoArgsConstructor
public abstract class Impuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="tipo_impuesto", nullable = false)
    private String tipoImpuesto;
    private String descripcion;
    private String empresa;
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Column(precision = 5, scale = 2)
    private BigDecimal porcentaje;
    @DecimalMin("0.00")
    @Column(name = "monto_base", precision = 12, scale = 2)
    private BigDecimal montoBase;
    private String numeroCliente;
    private String numeroMedidor;
    @DecimalMin("0.00")
    @Column(precision = 12, scale = 2)
    private BigDecimal montoAPagar;
    private LocalDate fechaFactura;
    private boolean estadoPago;
    @Column(name = "url_factura")
    private String urlFactura;

    @JsonBackReference // Anotación en la entidad "hijo"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recibo", nullable = false)
    private Recibo recibo;
}
