package com.backend.crmInmobiliario.entity.planesYSuscripciones;

import com.backend.crmInmobiliario.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Subscription {


    public enum Status {
        TRIALING, ACTIVE, PAST_DUE, CANCELED, UNPAID, INCOMPLETE, INCOMPLETE_EXPIRED,AUTHORIZED
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;


    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;


    @Enumerated(EnumType.STRING)
    Subscription.Status status;


    private String externalSubscriptionId;
    private String externalCustomerId;


    private LocalDateTime createdAt;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime updatedAt;
    private LocalDateTime trialEndsAt;

    @Column(name = "cancel_at_period_end")
    private Boolean cancelAtPeriodEnd;




}



