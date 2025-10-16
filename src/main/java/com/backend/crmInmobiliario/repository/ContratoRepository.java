package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Inquilino;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.backend.crmInmobiliario.entity.Usuario;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {


    Optional<Contrato> findByInquilinoAndActivoTrue(Inquilino inquilino);

    @Query("SELECT c FROM Contrato c WHERE c.usuario.username = :username")
    List<Contrato> findContratosByUsername(@Param("username") String username);

    List<Contrato> findByUsuario(Usuario usuario);

    @Query("SELECT c FROM Contrato c ORDER BY c.publicDate DESC")
    Page<Contrato> findLatestContratos(Pageable pageable);

    @Query("SELECT c FROM Contrato c LEFT JOIN FETCH c.recibos")
    List<Contrato> findAllWithRecibos();

    @Query("SELECT c FROM Contrato c LEFT JOIN FETCH c.garantes WHERE c.id = :id")
    Contrato findContratoByIdWithGarantes(@Param("id") Long id);


    int countByUsuarioUsername(String username);

    @Query("select count(c) from Contrato c where c.usuario.id=:usuarioId and c.activo=true")
    long countActivosByUsuario(@Param("usuarioId") Long usuarioId);

    Optional<Contrato> findByInquilinoId(Long inquilinoId);


    @Query("""
    SELECT DISTINCT c FROM Contrato c
    LEFT JOIN FETCH c.propietario
    LEFT JOIN FETCH c.inquilino
    LEFT JOIN FETCH c.propiedad
    LEFT JOIN FETCH c.usuario
    WHERE LOWER(c.nombreContrato) = LOWER(:nombre)
""")
    Optional<Contrato> findByNombreContratoCompleto(@Param("nombre") String nombre);

}


