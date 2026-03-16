package com.backend.crmInmobiliario.repository;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteUser;
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


    @Query("SELECT g FROM Garante g WHERE g.dni = :dni OR g.email = :email")
    Optional<Garante> findByDniOrEmail(@Param("dni") String dni, @Param("email") String email);

    Optional<Garante> findByUsuarioCuentaGaranteId(Long usuarioId);

    @Query("""
    SELECT g FROM Garante g
    WHERE g.usuarioCuentaGarante.username = :dato
       OR g.usuarioCuentaGarante.email = :dato
""")
    Optional<Garante> findByUsuarioCuentaGaranteUsernameOrEmail(@Param("dato") String dato);

    @Query("SELECT u.username AS username, u.password AS password FROM Usuario u JOIN u.garante g WHERE g.id = :garanteId")
    Optional<GaranteUser> obtenerCredencialesPorGarante(@Param("garanteId") Long garanteId);

    @Modifying
    @Query("UPDATE Garante g SET g.usuarioCuentaGarante = NULL WHERE g.usuarioCuentaGarante.id = :usuarioId")
    void desvincularUsuarioCuentaGarante(@Param("usuarioId") Long usuarioId);

    Page<Garante> findAllByUsuario_Id(Long userId, Pageable pageable);
}
