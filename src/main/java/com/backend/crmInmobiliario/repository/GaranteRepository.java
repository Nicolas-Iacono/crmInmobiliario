package com.backend.crmInmobiliario.repository;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Garante;
import com.backend.crmInmobiliario.entity.Propietario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GaranteRepository extends JpaRepository<Garante, Long> {

    @Modifying
    @Query("DELETE FROM Garante g WHERE g.contrato.id = :contratoId")
    void deleteByContratoId(@Param("contratoId") Long contratoId);

    @Query("SELECT g FROM Garante g WHERE g.usuario.username = :username")
    List<Garante> findGaranteByUsername(@Param("username") String username);

    @Query("SELECT g FROM Garante g WHERE g.usuario.id = :userId")
    List<Garante> findByUsuarioId(@Param("userId") Long userId);

    int countByUsuarioId(Long userId);

    List<Garante> findByNombreLikeIgnoreCaseOrApellidoLikeIgnoreCaseAndUsuarioId(
            String nombre, String apellido, Long userId);

    Optional<Garante> findByDniAndUsuarioId(String dni, Long userId);

    Optional<Garante> findByEmailIgnoreCaseAndUsuarioId(String email, Long userId);

    @Query("""
    SELECT g FROM Garante g
    WHERE g.contrato.id = :id
""")
    List<Garante> findGarantesByContratoId(Long id);

    Page<Garante> findAllByUsuario_Id(Long userId, Pageable pageable);
}
