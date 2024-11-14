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

    @Query("SELECT i FROM Inquilino i WHERE i.usuario.username = :username")
    List<Inquilino> findInquilinoByUsername(@Param("username") String username);
}
