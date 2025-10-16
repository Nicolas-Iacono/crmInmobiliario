package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.entity.IngresoMensual;
import com.backend.crmInmobiliario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.backend.crmInmobiliario.entity.Contrato;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface IngresoMensualRepository extends JpaRepository<IngresoMensual, Long> {


    // 🔹 Buscar todos los ingresos de un mes y año específico
    List<IngresoMensual> findByMesAndAnio(int mes, int anio);

    // 🔹 Buscar ingresos de un usuario en un mes y año
    List<IngresoMensual> findByUsuarioAndMesAndAnio(Usuario usuario, int mes, int anio);

    // 🔹 Buscar todos los ingresos de un usuario
    List<IngresoMensual> findByUsuario(Usuario usuario);

    // 🔹 Buscar ingresos por contrato
    @Query("SELECT i FROM IngresoMensual i WHERE i.contrato.id_contrato = :idContrato")
    List<IngresoMensual> findByContratoId(@Param("idContrato") Long idContrato);


    // 🔹 Verificar si ya se registraron ingresos ese mes/año (para evitar duplicados)
    Optional<IngresoMensual> findFirstByMesAndAnio(int mes, int anio);

    // 🔹 Consulta para totales mensuales (por usuario)
    @Query("""
        SELECT SUM(i.ingresoCalculadoPorMes)
        FROM IngresoMensual i
        WHERE i.usuario = :usuario AND i.mes = :mes AND i.anio = :anio
    """)
    Optional<Double> obtenerTotalMensualPorUsuario(Usuario usuario, int mes, int anio);

    // 🔹 Consulta general para totales agrupados por mes y año (para gráficos)
    @Query("""
    SELECT i.anio, i.mes,
           SUM(i.ingresoCalculadoPorMes),
           SUM(i.ingresoCalculadoPorContrato)
    FROM IngresoMensual i
    WHERE i.usuario = :usuario AND i.anio = :anio
    GROUP BY i.anio, i.mes
    ORDER BY i.anio, i.mes
""")
    List<Object[]> obtenerTotalesAgrupadosPorMes(Usuario usuario, int anio);

    boolean existsByContratoAndAnioAndMes(Contrato contrato, int anio, int mes);


}
