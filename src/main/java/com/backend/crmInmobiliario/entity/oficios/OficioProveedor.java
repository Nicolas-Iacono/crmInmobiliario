package com.backend.crmInmobiliario.entity.oficios;

import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "oficio_proveedor")
@Getter
@Setter
public class OficioProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false)
    private String nombreCompleto;

    private String empresa;
    private String emailContacto;
    private String telefonoContacto;
    private String descripcion;
    private String localidad;
    private String provincia;

    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @Column(nullable = false)
    private LocalDate periodoGraciaHasta;

    @ElementCollection
    @CollectionTable(name = "oficio_proveedor_categorias", joinColumns = @JoinColumn(name = "oficio_proveedor_id"))
    @Column(name = "categoria", nullable = false)
    private List<String> categorias = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "oficio_proveedor_imagenes_empresa", joinColumns = @JoinColumn(name = "oficio_proveedor_id"))
    @Column(name = "imagen_url", nullable = false)
    private List<String> imagenesEmpresa = new ArrayList<>();

    @Column(nullable = false)
    private Double promedioCalificacion = 0.0;

    @Column(nullable = false)
    private Integer totalCalificaciones = 0;
}
