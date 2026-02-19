package com.backend.crmInmobiliario.service.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.RegistroOficioProveedorDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IOficioProveedorService {
    List<CategoriaOficio> listarCategorias();
    OficioProveedorSalidaDto registrarProveedor(RegistroOficioProveedorDto dto);
    List<OficioProveedorSalidaDto> listarProveedoresVisibles();
    OficioProveedorSalidaDto agregarServicio(Long userId, OficioServicioEntradaDto dto, MultipartFile[] imagenes);
    List<OficioServicioSalidaDto> listarMisServicios(Long userId);
    OficioServicioSalidaDto editarServicio(Long userId, Long servicioId, OficioServicioEntradaDto dto, MultipartFile[] imagenes);
    void eliminarServicio(Long userId, Long servicioId);
    OficioProveedorSalidaDto actualizarImagenPerfilEmpresa(Long userId, MultipartFile archivo);
    OficioProveedorSalidaDto calificarProveedor(Long proveedorId, Long inmobiliariaId, OficioCalificacionEntradaDto dto);
    OficioProveedorSalidaDto asignarPlan(Long userId, Long planId);
}
