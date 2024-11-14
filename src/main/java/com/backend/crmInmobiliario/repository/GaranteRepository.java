package com.backend.crmInmobiliario.repository;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Garante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GaranteRepository extends JpaRepository<Garante, Long> {

    @Modifying
    @Query("DELETE FROM Garante g WHERE g.contrato.id = :contratoId")
    void deleteByContratoId(@Param("contratoId") Long contratoId);

    @Query("SELECT g FROM Garante g WHERE g.usuario.username = :username")
    List<Garante> findGaranteByUsername(@Param("username") String username);
}
