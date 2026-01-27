package com.backend.crmInmobiliario.repository.pagosYSuscripciones;

import com.backend.crmInmobiliario.entity.planesYSuscripciones.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByExternalSubscriptionId(String externalId);

    Optional<Subscription> findByUsuarioId(Long usuarioId);

    Optional<Subscription> findByExternalCustomerId(String externalCustomerId);

    Optional<Subscription> findByUsuarioIdAndStatus(Long usuarioId, String status);

    List<Subscription> findAllByStatus(Subscription.Status status);
}
