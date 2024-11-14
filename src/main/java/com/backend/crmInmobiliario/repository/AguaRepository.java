package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.impuestos.Agua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AguaRepository extends JpaRepository<Agua, Long> {
}
