package com.backend.crmInmobiliario.repository.USER_REPO;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {


    @Query("SELECT u FROM Usuario u WHERE u.username = :username")
    Optional<Usuario> findUserByUsername(@Param("username") String username);

    Optional<Usuario> findByEmail(String email);


    boolean existsByUsername(String username);

}
