package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoAguaEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.ImpuestoGasEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoAguaSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ImpuestoGasSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface IGasService {

    List<ImpuestoGasSalidaDto> listarImpuestoGas();
    ImpuestoGasSalidaDto crearImpuestoGas(ImpuestoGasEntradaDto impuestoGasEntradaDto) throws ResourceNotFoundException;
    void eliminarImpuestoGas(Long id)throws ResourceNotFoundException;
    ImpuestoGasSalidaDto buscarImpuestoPorId(Long id) throws ResourceNotFoundException;
}
