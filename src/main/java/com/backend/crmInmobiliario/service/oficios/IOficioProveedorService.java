package com.backend.crmInmobiliario.service.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioProveedorCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioProveedorUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
public interface IOficioProveedorService {
    OficioProveedorSalidaDto registrarProveedor(OficioProveedorCreateDto dto);
    OficioProveedorSalidaDto obtenerMiPerfil(Long userId);
    OficioProveedorSalidaDto actualizarMiPerfil(Long userId, OficioProveedorUpdateDto dto);
    void eliminarMiPerfil(Long userId); // si querés hard delete o soft delete

    @Transactional
    List<OficioServicioSalidaDto> listarMisServicios(Long userId);

    OficioProveedorSalidaDto actualizarImagenPerfil(Long userId, MultipartFile archivo) throws IOException;

    List<CategoriaOficio> listarCategorias();

    @Transactional

    List<OficioProveedorSalidaDto> listarProveedoresVisibles();
}
