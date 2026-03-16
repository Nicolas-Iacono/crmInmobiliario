package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.DTO.salida.visita.VisitaSalidaDto;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitaRepository extends JpaRepository<Visita, Long> {
    @Query("""
        SELECT v
        FROM Visita v
        WHERE v.propiedad.id_propiedad = :propiedadId
        ORDER BY v.fecha DESC, v.hora DESC
    """)
    List<Visita> findUltimasPorPropiedad(@Param("propiedadId") Long propiedadId);

    Optional<Visita> findByIdAndPropiedadUsuarioId(Long visitaId, Long usuarioId);

    List<Visita> findByPropiedadUsuarioIdOrderByFechaDescHoraDesc(Long usuarioId);


    @Query("""
  select new com.backend.crmInmobiliario.DTO.salida.visita.VisitaSalidaDto(
    v.id,
    v.propiedad.id_propiedad,
    v.titulo,
    v.fecha,
    v.hora,
    v.aclaracion,
    v.nombreCorredor,
    v.visitanteNombre,
    v.visitanteApellido,
    v.visitanteTelefono,
    p.id,
    concat(p.nombre, ' ', p.apellido)
  )
  from Visita v
  left join v.prospectoVisitante p
  where v.propiedad.id_propiedad = :propiedadId
  order by v.fecha desc, v.hora desc
""")
    List<VisitaSalidaDto> listarDtoPorPropiedad(@Param("propiedadId") Long propiedadId);
}
