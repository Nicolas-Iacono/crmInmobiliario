package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.PlantillaContrato;
import com.backend.crmInmobiliario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlantillaContratoRepository extends JpaRepository<PlantillaContrato, Long> {
    List<PlantillaContrato> findByUsuario(Usuario usuario);

    // 🔹 Trae todas las plantillas del usuario (inmobiliaria)
    @Query("SELECT p FROM PlantillaContrato p WHERE p.usuario.id = :usuarioId ORDER BY p.id DESC")
    List<PlantillaContrato> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    // 🔹 Buscar por nombre dentro de las del usuario
    @Query("SELECT p FROM PlantillaContrato p WHERE p.usuario.id = :usuarioId AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<PlantillaContrato> searchByNombre(@Param("usuarioId") Long usuarioId, @Param("nombre") String nombre);



}
