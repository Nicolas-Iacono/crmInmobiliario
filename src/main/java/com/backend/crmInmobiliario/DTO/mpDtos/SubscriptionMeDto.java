package com.backend.crmInmobiliario.DTO.mpDtos;

import java.time.Instant;
import java.time.LocalDateTime;


public record SubscriptionMeDto(
        String username,
        String planCode,
        String planName,
        String status,
        Integer contractLimit,
        long contratosActivos,
        Instant trialEndsAt,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd
) {}
