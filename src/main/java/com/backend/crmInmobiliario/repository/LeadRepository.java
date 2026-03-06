package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    boolean existsByCallId(String callId);
    List<Lead> findByUsuarioId(Long usuarioId);
}
