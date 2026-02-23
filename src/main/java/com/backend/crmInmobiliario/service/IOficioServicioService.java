package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.DTO.entrada.aliados.OficioProveedorCreateDto;
import com.backend.crmInmobiliario.DTO.entrada.aliados.OficioServicioCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.aliados.OficioServicioUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioServicioSalidaDto;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IOficioServicioService {
    @Transactional
    OficioProveedorSalidaDto registrarProveedor(OficioProveedorCreateDto dto);

    OficioServicioSalidaDto crear(Long userId, OficioServicioCreateDto dto, MultipartFile[] imagenes) throws IOException;

    List<OficioServicioSalidaDto> listarMisServicios(Long userId);

    OficioServicioSalidaDto obtenerMiServicio(Long userId, Long servicioId);

    OficioServicioSalidaDto editar(Long userId, Long servicioId, OficioServicioUpdateDto dto, MultipartFile[] imagenes) throws IOException;

    void eliminar(Long userId, Long servicioId);
}

