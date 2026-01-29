package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.prospecto.ProspectoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ProspectoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.prospecto.ProspectoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IProspectoService {
    ProspectoSalidaDto crearProspecto(Long usuarioId, ProspectoEntradaDto dto) throws ResourceNotFoundException;

    ProspectoSalidaDto actualizarProspecto(Long usuarioId, Long id, ProspectoModificacionDto dto) throws ResourceNotFoundException;

    List<ProspectoSalidaDto> listarProspectosPorUsuario(Long usuarioId) throws ResourceNotFoundException;

    void eliminarProspecto(Long usuarioId, Long id) throws ResourceNotFoundException;

    List<PropiedadSalidaDto> listarPropiedadesCompatibles(Long usuarioId, Long prospectoId) throws ResourceNotFoundException;

    void notificarPropiedadCompatible(Long usuarioId, Long prospectoId, Long propiedadId) throws ResourceNotFoundException;
}
