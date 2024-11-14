package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.garante.GaranteEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IGaranteService {


    List<GaranteSalidaDto> listarGarantes();

    GaranteSalidaDto crearGarante(GaranteEntradaDto garanteEntradaDto) throws ResourceNotFoundException;

    GaranteSalidaDto listarGarantePorId(Long id) throws ResourceNotFoundException;

    void eliminarGarante(Long id) throws ResourceNotFoundException;

    void asignarGarante(Long idGarante, Long idContrato) throws ResourceNotFoundException;

//    void agregarDni(Long id, MultipartFile archivoDni) throws ResourceNotFoundException, IOException;
//
//    void agregarRecibo(Long id,MultipartFile archivoRecibo) throws ResourceNotFoundException,  IOException;
public void deleteByContratoId(Long contratoId);

    List<GaranteSalidaDto> buscarGarantePorUsuario(String username);
}
