package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.visita.VisitaEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.visita.VisitaModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.visita.VisitaSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IVisitaService {
    VisitaSalidaDto crearVisita(VisitaEntradaDto dto) throws ResourceNotFoundException;

    VisitaSalidaDto actualizarVisita(Long visitaId, VisitaModificacionDto dto) throws ResourceNotFoundException;

    VisitaSalidaDto buscarVisita(Long visitaId) throws ResourceNotFoundException;

    List<VisitaSalidaDto> listarVisitasPorPropiedad(Long propiedadId);

    void eliminarVisita(Long visitaId) throws ResourceNotFoundException;
}
