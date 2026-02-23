package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "oficio_resena",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_resena_usuario_proveedor",
                columnNames = {"usuario_id", "proveedor_id"}
        )
)
@Data
@NoArgsConstructor
public class OficioResena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // inmobiliaria que opina

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private OficioProveedor proveedor;

    @Column(nullable = false)
    private Integer calificacion; // 1..5

    @Column(length = 2000)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // getters/setters
}
