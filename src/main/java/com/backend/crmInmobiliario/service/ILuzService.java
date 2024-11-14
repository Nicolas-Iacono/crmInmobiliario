package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoGasEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.ImpuestoLuzEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoGasSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoLuzSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ILuzService {
    List<ImpuestoLuzSalidaDto> listarImpuestoLuz();
    ImpuestoLuzSalidaDto crearImpuestoLuz(ImpuestoLuzEntradaDto impuestoLuzEntradaDto) throws ResourceNotFoundException;
    void eliminarImpuestoLuz(Long id)throws ResourceNotFoundException;
    ImpuestoLuzSalidaDto buscarImpuestoPorId(Long id) throws ResourceNotFoundException;
}
