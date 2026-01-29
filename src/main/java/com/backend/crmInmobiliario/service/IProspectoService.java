package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.prospecto.ProspectoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ProspectoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.prospecto.ProspectoSalidaDto;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;

public interface IProspectoService {
    ProspectoSalidaDto crearProspecto(ProspectoEntradaDto dto) throws ResourceNotFoundException;

    ProspectoSalidaDto actualizarProspecto(Long id, ProspectoModificacionDto dto) throws ResourceNotFoundException;

    List<ProspectoSalidaDto> listarMisProspectos();

    void eliminarProspecto(Long id) throws ResourceNotFoundException;

    void notificarProspectosCompatiblesPorPropiedad(Propiedad propiedad)throws ResourceNotFoundException;

    @Transactional
    List<ProspectoSalidaDto> listarProspectosCompatibles(Long propiedadId, Long userId);
}
