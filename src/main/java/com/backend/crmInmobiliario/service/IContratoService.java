package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoRenovacionDtoEntrada;
import com.backend.crmInmobiliario.DTO.modificacion.ContratoComisionDtoSalida;
import com.backend.crmInmobiliario.DTO.modificacion.ContratoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IContratoService {
    List<ContratoSalidaDto> listarContratos() ;
    ContratoSalidaDto crearContrato(ContratoEntradaDto contratoEntradaDto) throws ResourceNotFoundException;

    List<ContratoSalidaDto> buscarContratoPorUsuario(String username);

    ContratoSalidaDto guardarContratoPdf(Long contratoId, ContratoModificacionDto actualizacion)throws ResourceNotFoundException;
    ContratoSalidaDto buscarContratoPorId(Long id);
    void eliminarContrato(Long id) throws ResourceNotFoundException;

    Boolean cambiarEstadoContrato(Long id) throws ResourceNotFoundException;

    void finalizarContrato (Long id)throws ResourceNotFoundException;

    ContratoActualizacionDtoSalida verificarActualizacionContrato(Long id) throws ResourceNotFoundException;

    Long verificarFinalizacionContrato(Long id) throws ResourceNotFoundException;

    void verificarAlertasContratos();

    List<ContratoVencimientoAlertaDto> obtenerAlertasVencimiento(int diasAviso);

    ContratoSalidaDto renovarContrato(ContratoRenovacionDtoEntrada dto) throws ResourceNotFoundException;

    List<LatestContratosSalidaDto> getLatestContratos();

    Integer enumerarContratos(String username)throws  ResourceNotFoundException;

    ContratoSalidaDto actualizarMontoAlquiler(ContratoModificacionDto contratoModificacionDto) throws  ResourceNotFoundException;

}
