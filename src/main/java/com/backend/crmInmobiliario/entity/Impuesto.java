package com.backend.crmInmobiliario.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private int porcentaje;
    private String numeroCliente;
    private String numeroMedidor;
    private Double montoAPagar;
    private LocalDate fechaFactura;
    private boolean estadoPago;


    @JsonBackReference // Anotación en la entidad "hijo"
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recibo", nullable = false)
    private Recibo recibo;
}
