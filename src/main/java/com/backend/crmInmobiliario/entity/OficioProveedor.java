package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "oficio_proveedor")
@Getter @Setter
public class OficioProveedor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String nombreCompleto;

    private String empresa;
    private String emailContacto;
    private String telefonoContacto;

    @Column(length = 2000)
    private String descripcion;

    private String localidad;
    private String provincia;

    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @ElementCollection
    @CollectionTable(
            name = "oficio_proveedor_categorias",
            joinColumns = @JoinColumn(name = "proveedor_id") // <-- TU DB
    )
    @Column(name = "categoria")
    private Set<String> categorias = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_perfil_id")
    private ImageUrls imagenPerfil;

    @Column(name = "periodo_gracia_hasta", nullable = true)
    private LocalDateTime periodoGraciaHasta;

    // agregados/derivados
    @Column(nullable = false)
    private Double promedioCalificacion = 0.0;

    @Column(nullable = false)
    private Integer totalCalificaciones = 0;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OficioResena> resenas = new ArrayList<>();

}