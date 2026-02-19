package com.backend.crmInmobiliario.entity.oficios;

import com.backend.crmInmobiliario.entity.ImageUrls;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "oficio_servicio")
@Getter @Setter
public class OficioServicio {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "oficio_proveedor_id", nullable = false)
    private OficioProveedor proveedor;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 4000)
    private String descripcion;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioDesdeArs;

    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    // Imágenes del trabajo/servicio (urls simples)
    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImageUrls> imagenes = new ArrayList<>();

    @Column(nullable = false)
    private boolean activo = true;
}
