package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "visita")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospecto_id")
    private Prospecto prospectoVisitante;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, updatable = false)
    private LocalDate fecha;

    @Column(nullable = false, updatable = false)
    private LocalTime hora;

    @Column(length = 2000)
    private String aclaracion;

    @Column(nullable = false)
    private String nombreCorredor;

    private String visitanteNombre;
    private String visitanteApellido;
    private String visitanteTelefono;
}
