package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Locale;

@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class Impuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String descripcion;
    private String empresa;
    private Double porcentaje;
    private String numeroCliente;
    private String numeroMedidor;
    private Double montoAPagar;
    private LocalDate fechaFactura;
    private Boolean estadoPago;
}
