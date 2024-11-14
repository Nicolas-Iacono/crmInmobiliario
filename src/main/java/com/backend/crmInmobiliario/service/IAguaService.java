package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoAguaEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoAguaSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IAguaService {

    List<ImpuestoAguaSalidaDto> listarImpuestoAgua();
    ImpuestoAguaSalidaDto crearImpuestoAgua(ImpuestoAguaEntradaDto impuestoAguaEntradaDto) throws ResourceNotFoundException;
    void eliminarImpuestoAgua(Long id)throws ResourceNotFoundException;
    ImpuestoAguaSalidaDto buscarImpuestoPorId(Long id) throws ResourceNotFoundException;

}
