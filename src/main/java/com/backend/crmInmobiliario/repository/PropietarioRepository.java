package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {

    Optional<Propietario> findByNombreAndApellido(String nombre, String apellido);

    @Query("SELECT p FROM Propietario p WHERE p.usuario.username = :username")
    List<Propietario> findPropietarioByUsername(@Param("username") String username);

    int countByUsuarioUsername(String username);
}
