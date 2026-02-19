package com.backend.crmInmobiliario.service.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioProveedorCreateDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioProveedorUpdateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioServicioUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IOficioProveedorService {
    List<CategoriaOficio> listarCategorias();

    OficioProveedorSalidaDto registrarProveedor(OficioProveedorCreateDto dto);

    OficioProveedorSalidaDto obtenerMiPerfil(Long userId);

    OficioProveedorSalidaDto actualizarMiPerfil(Long userId, OficioProveedorUpdateDto dto);

    void eliminarMiPerfil(Long userId);

    List<OficioProveedorSalidaDto> listarProveedoresVisibles();

    OficioProveedorSalidaDto asignarPlan(Long userId, Long planId);

    OficioProveedorSalidaDto actualizarImagenPerfil(Long userId, MultipartFile archivo) throws IOException;

    OficioServicioSalidaDto agregarServicio(Long userId, OficioServicioCreateDto dto, MultipartFile[] imagenes) throws IOException;

    List<OficioServicioSalidaDto> listarMisServicios(Long userId);

    OficioServicioSalidaDto editarServicio(Long userId, Long servicioId, OficioServicioUpdateDto dto, MultipartFile[] nuevasImagenes) throws IOException;

    void eliminarServicio(Long userId, Long servicioId);

    OficioProveedorSalidaDto calificarProveedor(Long proveedorId, Long inmobiliariaId, OficioCalificacionEntradaDto dto);
}
