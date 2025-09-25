package com.backend.crmInmobiliario.entity.planesYSuscripciones;

import com.backend.crmInmobiliario.entity.Usuario;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario; // monousuario: una subs por usuario

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Status status;     // ACTIVE, TRIALING, PAST_DUE, CANCELED

    private Instant trialEndsAt;        // Free trial o período promocional
    private Instant currentPeriodEnd;   // fin del mes actual
    private Boolean cancelAtPeriodEnd;  // true si cancela al final del ciclo

    // IDs del procesador de pagos
    private String externalCustomerId;
    private String externalSubscriptionId;

    private Instant createdAt;
    private Instant updatedAt;

    public enum Status { TRIALING, ACTIVE, PAST_DUE, CANCELED }
}
