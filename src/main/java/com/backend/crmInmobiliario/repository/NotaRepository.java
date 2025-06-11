package com.backend.crmInmobiliario.repository;

import com.backend.crmInmobiliario.DTO.salida.NotaSalidaDto;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaRepository extends JpaRepository<Nota,Long> {


}
