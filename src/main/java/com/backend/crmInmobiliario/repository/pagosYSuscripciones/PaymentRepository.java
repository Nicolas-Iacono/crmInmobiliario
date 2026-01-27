package com.backend.crmInmobiliario.repository.pagosYSuscripciones;

import com.backend.crmInmobiliario.entity.planesYSuscripciones.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMpPaymentId(String mpPaymentId);
    List<Payment> findByUsuarioUsernameOrderByPaymentDateDesc(String username);
}
