package com.backend.crmInmobiliario.repository.notificacionesPush;

import com.backend.crmInmobiliario.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUserId(Long userId);
}
