package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.propiedades.PropiedadEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSoloSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IPropiedadService {
    List<PropiedadSoloSalidaDto> listarPropiedades();

    PropiedadSalidaDto crearPropiedad (PropiedadEntradaDto propiedadEntradaDto, Long propietarioId) throws ResourceNotFoundException;

    PropiedadSalidaDto buscarPropiedadPorId(Long id) throws ResourceNotFoundException;

    void eliminarPropiedad(Long id) throws ResourceNotFoundException;

    Boolean cambiarDisponibilidadPropiedad(Long id) throws ResourceNotFoundException;

    List<PropiedadSalidaDto> buscarPropiedadesPorUsuario(String username);

}
