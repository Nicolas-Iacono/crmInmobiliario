package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Garante;
import com.backend.crmInmobiliario.entity.Inquilino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InquilinoRepository extends JpaRepository<Inquilino, Long> {

    Optional<Inquilino> findByNombreAndApellido(String nombre, String apellido);

    int countByUsuarioUsername(String username);

    @Query("SELECT i FROM Inquilino i WHERE i.dni = :dni OR i.email = :email")
    Optional<Inquilino> findByDniOrEmail(@Param("dni") String dni, @Param("email") String email);

    Optional<Inquilino> findByUsuarioId(Long usuarioId);

    Optional<Inquilino> findByUsuarioCuentaInquilinoId(Long usuarioId);

    @Query("SELECT i FROM Inquilino i WHERE i.usuario.username = :username")
    List<Inquilino> findInquilinoByUsername(@Param("username") String username);

    @Query("SELECT i FROM Inquilino i WHERE i.usuarioCuentaInquilino.username = :username")
    Optional<Inquilino> findByUsuarioCuentaInquilinoUsername(@Param("username") String username);

    @Query("""
    SELECT i FROM Inquilino i
    WHERE i.usuarioCuentaInquilino.username = :dato
       OR i.usuarioCuentaInquilino.email = :dato
""")
    Optional<Inquilino> findByUsuarioCuentaInquilinoUsernameOrEmail(@Param("dato") String dato);


}
