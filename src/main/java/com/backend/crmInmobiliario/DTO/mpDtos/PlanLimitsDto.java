package com.backend.crmInmobiliario.DTO.mpDtos;

import com.backend.crmInmobiliario.entity.planesYSuscripciones.Subscription;


import java.time.Instant;
import java.time.LocalDateTime;


public record PlanLimitsDto(
        String planCode,
        String planName,
        Integer contractLimit,
        Subscription.Status status,
        Instant trialEndsAt,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd
) {}
