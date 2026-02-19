package com.backend.crmInmobiliario.service.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioServicioUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface IOficioServicioService {
    OficioServicioSalidaDto crearServicio(Long userId, OficioServicioCreateDto dto, MultipartFile[] imagenes);
    List<OficioServicioSalidaDto> listarMisServicios(Long userId);
    OficioServicioSalidaDto obtenerMiServicio(Long userId, Long servicioId);
    OficioServicioSalidaDto actualizarServicio(Long userId, Long servicioId, OficioServicioUpdateDto dto, MultipartFile[] imagenes);
    void eliminarServicio(Long userId, Long servicioId);

    // público
    OficioServicioSalidaDto obtenerServicioPublico(Long servicioId);
    List<OficioServicioSalidaDto> listarServiciosPorProveedor(Long proveedorId);
}
