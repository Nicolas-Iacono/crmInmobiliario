package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioUser;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Propietario;
import com.backend.crmInmobiliario.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

    Optional<Propietario> findByNombreAndApellido(String nombre, String apellido);

    @Query("SELECT p FROM Propietario p WHERE p.usuario.username = :username")
    List<Propietario> findPropietarioByUsername(@Param("username") String username);

    int countByUsuarioUsername(String username);

    @Query("SELECT i FROM Propietario i WHERE i.dni = :dni OR i.email = :email")
    Optional<Propietario> findByDniOrEmail(@Param("dni") String dni, @Param("email") String email);
    Optional<Propietario> findByEmail(String email);

    Optional<Propietario> findById(Long id);
    @Modifying
    @Query("UPDATE Propietario p SET p.usuarioCuentaPropietario = NULL WHERE p.usuarioCuentaPropietario.id = :usuarioId")
    void desvincularUsuarioCuentaPropietario(@Param("usuarioId") Long usuarioId);

    List<Propietario> findByUsuarioId(Long userId);

    int countByUsuarioId(Long userId);

    List<Propietario> findByNombreLikeIgnoreCaseOrApellidoLikeIgnoreCaseAndUsuarioId(
            String nombre, String apellido, Long userId);

    Optional<Propietario> findByDniAndUsuarioId(String dni, Long userId);

    Optional<Propietario> findByEmailIgnoreCaseAndUsuarioId(String email, Long userId);

    Page<Propietario> findAllByUsuario_Id(Long userId, Pageable pageable);

    Optional<Propietario> findByUsuarioCuentaPropietarioId(Long usuarioId);

}
