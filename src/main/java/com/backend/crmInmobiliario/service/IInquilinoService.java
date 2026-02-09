package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.InquilinoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.InquilinoDtoModificacion;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoUser;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioUser;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IInquilinoService {

    List<InquilinoSalidaDto> listarInquilinos() ;
    InquilinoSalidaDto crearInquilino(InquilinoEntradaDto inquilinoEntradaDto) throws ResourceNotFoundException;
    InquilinoSalidaDto buscarInquilinoPorId(Long id) throws ResourceNotFoundException;
    void eliminarInquilino(Long id) throws ResourceNotFoundException;

    List<InquilinoSalidaDto> buscarInquilinoPorUsuario(String username);

    Integer enumerarInquilinos();

    InquilinoSalidaDto editarInquilino(InquilinoDtoModificacion inquilinoDtoModificacion) throws ResourceNotFoundException;

    InquilinoUser listarCredenciales(Long propietarioId) throws ResourceNotFoundException;

    List<InquilinoSalidaDto> listarInquilinosPorUsuarioId(Long userId);
}
