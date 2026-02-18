package com.backend.crmInmobiliario.service.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.RegistroOficioProveedorDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IOficioProveedorService {
    List<CategoriaOficio> listarCategorias();
    OficioProveedorSalidaDto registrarProveedor(RegistroOficioProveedorDto dto);
    List<OficioProveedorSalidaDto> listarProveedoresVisibles();
    OficioProveedorSalidaDto agregarServicio(Long userId, OficioServicioEntradaDto dto);
    OficioProveedorSalidaDto calificarProveedor(Long proveedorId, Long inmobiliariaId, OficioCalificacionEntradaDto dto);
    OficioProveedorSalidaDto asignarPlan(Long userId, Long planId);
    ImgUrlSalidaDto subirLogoProveedor(Long userId, MultipartFile archivo);
    OficioProveedorSalidaDto subirImagenesEmpresa(Long userId, MultipartFile[] archivos);
    OficioProveedorSalidaDto subirImagenesTrabajo(Long userId, Long servicioId, MultipartFile[] archivos);
}
