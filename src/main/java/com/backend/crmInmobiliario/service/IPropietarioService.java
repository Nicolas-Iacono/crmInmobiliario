package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.PropietarioEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.PropietarioDtoModificacion;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioUser;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IPropietarioService {
    List<PropietarioSalidaDto> listarPropietarios();
    PropietarioSalidaDto crearPropietario(PropietarioEntradaDto propietarioEntradaDto) throws ResourceNotFoundException;
    PropietarioSalidaDto buscarPropietarioPorId(Long id) throws ResourceNotFoundException;
    void eliminarPropietario(Long id) throws ResourceNotFoundException;

    List<PropietarioSalidaDto> buscarPropietariosPorUsuario(String username);
    Integer enumerarPropietarios(String username);
    PropietarioSalidaDto editarPropietario(PropietarioDtoModificacion propietarioDtoModificacion ) throws ResourceNotFoundException;

    Object[] obtenerCredencialesPorPropietario(Long propietarioId);

    PropietarioUser listarCredenciales(Long propietarioId)throws ResourceNotFoundException;

    List<PropietarioSalidaDto> listarPropietariosPorUsuarioId(Long userId);
}
