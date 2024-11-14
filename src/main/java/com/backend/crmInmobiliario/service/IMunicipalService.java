package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoLuzEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.ImpuestoMunicipalEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoLuzSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoMunicipalSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IMunicipalService {
    List<ImpuestoMunicipalSalidaDto> listarImpuestoMunicipal();
    ImpuestoMunicipalSalidaDto crearImpuestoMunicipal(ImpuestoMunicipalEntradaDto impuestoMunicipalEntradaDto) throws ResourceNotFoundException;
    void eliminarImpuestoMunicipal(Long id)throws ResourceNotFoundException;
    ImpuestoMunicipalSalidaDto buscarImpuestoPorId(Long id) throws ResourceNotFoundException;
}
