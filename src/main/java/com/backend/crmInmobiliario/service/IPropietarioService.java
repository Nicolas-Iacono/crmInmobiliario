package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.PropietarioEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IPropietarioService {
    List<PropietarioSalidaDto> listarPropietarios();
    PropietarioSalidaDto crearPropietario(PropietarioEntradaDto propietarioEntradaDto) throws ResourceNotFoundException;
    PropietarioSalidaDto buscarPropietarioPorId(Long id) throws ResourceNotFoundException;
    void eliminarPropietario(Long id) throws ResourceNotFoundException;

    List<PropietarioSalidaDto> buscarPropietariosPorUsuario(String username);

}
