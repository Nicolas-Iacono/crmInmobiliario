package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.impuestos.Gas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GasRepository extends JpaRepository<Gas,Long> {
}
