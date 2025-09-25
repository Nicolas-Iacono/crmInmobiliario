package com.backend.crmInmobiliario.service;


import com.backend.crmInmobiliario.DTO.entrada.PresupuestoEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.PresupuestoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IPresupuestoService {
    PresupuestoSalidaDto crear(PresupuestoEntradaDto dto) throws ResourceNotFoundException;
    PresupuestoSalidaDto actualizar(Long id, PresupuestoEntradaDto dto) throws ResourceNotFoundException;
    PresupuestoSalidaDto buscarPorId(Long id) throws ResourceNotFoundException;
    List<PresupuestoSalidaDto> listar();
    void eliminar(Long id) throws ResourceNotFoundException;
    List<PresupuestoSalidaDto> listarPorUsuario(String username);
}