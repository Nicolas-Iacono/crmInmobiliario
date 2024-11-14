package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.ContratoPdfEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.ContratoPdfSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSoloSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IPdfService {

    ContratoPdfSalidaDto guardarPdf (ContratoPdfEntradaDto contratoPdfEntradaDto)  throws ResourceNotFoundException;
    void eliminarPdf(Long id)  throws ResourceNotFoundException;

    List<ContratoPdfSalidaDto> listarPdf();
}
