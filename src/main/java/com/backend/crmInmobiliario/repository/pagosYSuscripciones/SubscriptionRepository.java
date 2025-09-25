package com.backend.crmInmobiliario.repository.pagosYSuscripciones;

import com.backend.crmInmobiliario.entity.planesYSuscripciones.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUsuarioId(Long usuarioId);
    Optional<Subscription> findByExternalSubscriptionId(String externalId);
}
