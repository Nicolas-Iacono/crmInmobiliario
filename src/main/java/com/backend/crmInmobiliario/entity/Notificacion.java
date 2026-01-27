package com.backend.crmInmobiliario.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "notificacion")
@Data
@NoArgsConstructor
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="usuario_id", nullable=false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="contrato_id", nullable=false)
    private Contrato contrato;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TipoNotificacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    @Column(nullable=false, length=500)
    private String mensaje;

    private LocalDate fechaCreacion = LocalDate.now();
}
