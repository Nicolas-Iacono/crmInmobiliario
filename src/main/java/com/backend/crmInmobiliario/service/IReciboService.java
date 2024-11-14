//package com.backend.crmInmobiliario.service;
//
//
//import com.backend.crmInmobiliario.DTO.entrada.PropietarioEntradaDto;
//import com.backend.crmInmobiliario.DTO.entrada.ReciboEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
//import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
//import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//@Service
//public interface IReciboService {
//
//    List<ReciboSalidaDto> listarRecibos();
//    ReciboSalidaDto crearRecibo(ReciboEntradaDto reciboEntradaDto) throws ResourceNotFoundException;
//    ReciboSalidaDto buscarReciboPorId(Long id) throws ResourceNotFoundException;
//    void eliminarRecibo(Long id) throws ResourceNotFoundException;
//
//    List<ReciboSalidaDto> buscarRecibosPorNumDeContrato(Long id)throws ResourceNotFoundException;
//}
