package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Inquilino;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {


    Optional<Contrato> findByInquilinoAndActivoTrue(Inquilino inquilino);

    @Query("SELECT c FROM Contrato c WHERE c.usuario.username = :username")
    List<Contrato> findContratosByUsername(@Param("username") String username);

    @Query("SELECT c FROM Contrato c ORDER BY c.publicDate DESC")
    Page<Contrato> findLatestContratos(Pageable pageable);

}
