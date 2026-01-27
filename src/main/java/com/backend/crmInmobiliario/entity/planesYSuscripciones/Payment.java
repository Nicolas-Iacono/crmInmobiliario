package com.backend.crmInmobiliario.entity.planesYSuscripciones;


import com.backend.crmInmobiliario.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "mp_payment_id", unique = true)
    private String mpPaymentId; // ID del pago en Mercado Pago (authorized_payment_id)

    @Column(name = "preapproval_id")
    private String preapprovalId; // ID de la suscripción de MP

    @Column(name = "status")
    private String status; // ej: approved, rejected, pending

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 5)
    private String currency;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
