package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropiedadRepository extends JpaRepository<Propiedad,Long> {
    Optional<Propiedad> findByDireccionAndLocalidadAndPartidoAndProvincia(String direccion, String localidad, String partido, String provincia);;

    @Query("SELECT p FROM Propiedad p WHERE p.usuario.username = :username")
    List<Propiedad> findPropiedadByUsername(@Param("username") String username);
    int countByUsuarioUsername(String username);

}
