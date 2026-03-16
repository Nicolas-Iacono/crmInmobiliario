package com.backend.crmInmobiliario.repository.notificacionesPush;

import com.backend.crmInmobiliario.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUserId(Long userId);
    long countByUserId(Long userId);

    Optional<PushSubscription> findByEndpoint(String endpoint);
    Optional<PushSubscription> findFirstByUserIdOrderByCreatedAtAsc(Long userId);
}
