package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.impuestos.Municipal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MunicipalRepository extends JpaRepository<Municipal, Long> {
}
