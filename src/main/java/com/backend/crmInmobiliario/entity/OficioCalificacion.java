package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "oficio_calificacion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"proveedor_id", "inmobiliaria_user_id"})
)
@Getter
@Setter
public class OficioCalificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private OficioProveedor proveedor;

    // inmobiliaria que califica (tu Usuario logueado)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inmobiliaria_user_id", nullable = false)
    private Usuario inmobiliaria;

    @Column(nullable = false)
    private Integer estrellas; // 1..5

    @Column(length = 1000)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}

