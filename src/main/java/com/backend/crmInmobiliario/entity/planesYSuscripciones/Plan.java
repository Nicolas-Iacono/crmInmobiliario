package com.backend.crmInmobiliario.entity.planesYSuscripciones;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@NoArgsConstructor
@Data
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "price_usd", nullable = false)
    private BigDecimal priceUsd;

    @Column(name = "contract_limit", nullable = false)
    private Integer contractLimit;

    @Column(nullable = false, unique = true)
    private String code; // ej: FREE, STARTER, PRO

    @Column(nullable = false)
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    // getters/setters
}
