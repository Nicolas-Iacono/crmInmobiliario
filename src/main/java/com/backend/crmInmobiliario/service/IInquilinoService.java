package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.InquilinoEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;

import java.util.List;

public interface IInquilinoService {

    List<InquilinoSalidaDto> listarInquilinos() ;
    InquilinoSalidaDto crearInquilino(InquilinoEntradaDto inquilinoEntradaDto) throws ResourceNotFoundException;
    InquilinoSalidaDto buscarInquilinoPorId(Long id) throws ResourceNotFoundException;
    void eliminarInquilino(Long id) throws ResourceNotFoundException;

    List<InquilinoSalidaDto> buscarInquilinoPorUsuario(String username);

    Integer enumerarInquilinos(String username);

}
