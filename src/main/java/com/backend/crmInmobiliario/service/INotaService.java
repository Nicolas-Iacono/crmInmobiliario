package com.backend.crmInmobiliario.service;


import com.backend.crmInmobiliario.DTO.entrada.NotaEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.NotaModificacionDto;
import com.backend.crmInmobiliario.DTO.modificacion.ReciboModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.NotaSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.LatestContratosSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface INotaService {

    List<NotaSalidaDto> listarNotas() ;
    NotaSalidaDto crearNota(NotaEntradaDto notaEntradaDto) throws ResourceNotFoundException;

    NotaSalidaDto buscarNotaPorId(Long id) throws ResourceNotFoundException;
    void eliminarNota(Long id) throws ResourceNotFoundException;
    NotaSalidaDto modificarEstado(NotaModificacionDto notaModificacionDto) throws ResourceNotFoundException;

}
