package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.impuestos.Luz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LuzRepository extends JpaRepository<Luz, Long> {
}
