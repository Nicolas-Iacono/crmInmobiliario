package com.backend.crmInmobiliario.entity.oficios;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "oficio_servicio")
@Getter
@Setter
public class OficioServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oficio_proveedor_id", nullable = false)
    private OficioProveedor proveedor;

    @Column(nullable = false)
    private String titulo;

    private String descripcion;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioDesdeArs;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioHastaArs;

    @ElementCollection
    @CollectionTable(name = "oficio_servicio_imagenes", joinColumns = @JoinColumn(name = "oficio_servicio_id"))
    @Column(name = "imagen_url", nullable = false)
    private List<String> imagenesTrabajos = new ArrayList<>();
}
