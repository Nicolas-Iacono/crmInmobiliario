package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Presupuesto;
import com.backend.crmInmobiliario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    @Query("""
           SELECT p
           FROM Presupuesto p
           JOIN FETCH p.usuario u
           WHERE u.username = :username
           """)
    List<Presupuesto> findByUsuario(@Param("username") String username);
}



