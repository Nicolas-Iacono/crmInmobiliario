package com.backend.crmInmobiliario.service.impl.aliados;

import com.backend.crmInmobiliario.DTO.salida.aliados.*;
import com.backend.crmInmobiliario.entity.OficioProveedor;
import com.backend.crmInmobiliario.entity.OficioServicio;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.aliados.OficioProveedorRepository;
import com.backend.crmInmobiliario.repository.aliados.OficioServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OficioMarketplaceService {

    private final OficioProveedorRepository proveedorRepository;
    private final OficioServicioRepository servicioRepository;

    public OficioMarketplaceService(OficioProveedorRepository proveedorRepository,
                                    OficioServicioRepository servicioRepository) {
        this.proveedorRepository = proveedorRepository;
        this.servicioRepository = servicioRepository;
    }

    // 1) Listado de proveedores
    @Transactional(readOnly = true)
    public List<OficioProveedorCardDto> listarProveedores() {
        return proveedorRepository.findAllWithCategoriasAndImagen()
                .stream()
                .map(this::toProveedorCardDto)
                .toList();
    }
    // 2) Servicios de un proveedor
    public List<OficioServicioPublicoDto> listarServiciosDeProveedor(Long proveedorId) {
        return servicioRepository.findServiciosByProveedorIdConImagenes(proveedorId)
                .stream()
                .map(this::toServicioPublicoDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public OficioProveedorDetalleDto obtenerDetalle(Long id) {

        OficioProveedor proveedor = proveedorRepository.findByIdWithResenas(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        return toDetalleDto(proveedor);
    }
    private OficioProveedorDetalleDto toDetalleDto(OficioProveedor p) {

        OficioProveedorDetalleDto dto = new OficioProveedorDetalleDto();
        dto.setId(p.getId());
        dto.setNombreCompleto(p.getNombreCompleto());
        dto.setDescripcion(p.getDescripcion());
        dto.setPromedioCalificacion(p.getPromedioCalificacion());
        dto.setTotalCalificaciones(p.getTotalCalificaciones());

        dto.setResenas(
                p.getResenas().stream().map(r -> {
                    OficioProveedorDetalleDto.ResenaSalidaDto rDto = new OficioProveedorDetalleDto.ResenaSalidaDto();
                    rDto.setId(r.getId());
                    rDto.setCalificacion(r.getCalificacion());
                    rDto.setComentario(r.getComentario());
                    rDto.setFechaCreacion(r.getFechaCreacion());
                    rDto.setUsuarioId(r.getUsuario().getId());
                    rDto.setUsername(r.getUsuario().getUsername());
                    rDto.setNombreNegocio(r.getUsuario().getNombreNegocio());
                    rDto.setUsuarioImagenPerfilUrl(r.getUsuario().getLogoInmobiliaria().getImageUrl());
                    return rDto;
                }).toList()
        );

        return dto;
    }

    // ---------------- mappers ----------------

    private OficioProveedorCardDto toProveedorCardDto(OficioProveedor p) {
        OficioProveedorCardDto dto = new OficioProveedorCardDto();
        dto.setId(p.getId());
        dto.setNombreCompleto(p.getNombreCompleto());
        dto.setEmpresa(p.getEmpresa());
        dto.setDescripcion(p.getDescripcion());
        dto.setTelefonoContacto(p.getTelefonoContacto());
        dto.setEmailContacto(p.getEmailContacto());
        dto.setProvincia(p.getProvincia());
        dto.setLocalidad(p.getLocalidad());
        dto.setPromedioCalificacion(p.getPromedioCalificacion());
        dto.setTotalCalificaciones(p.getTotalCalificaciones());

        if (p.getCategorias() == null || p.getCategorias().isEmpty()) {
            dto.setCategorias(List.of());
        } else {
            dto.setCategorias(new ArrayList<>(p.getCategorias()));
        }

        if (p.getImagenPerfil() != null) {
            dto.setImagenPerfilId(p.getImagenPerfil().getIdImage());
            dto.setImagenPerfilUrl(p.getImagenPerfil().getImageUrl());
        }

        return dto;
    }

    private OficioServicioPublicoDto toServicioPublicoDto(OficioServicio s) {
        OficioServicioPublicoDto dto = new OficioServicioPublicoDto();
        dto.setId(s.getId());
        dto.setTitulo(s.getTitulo());
        dto.setDescripcion(s.getDescripcion());
        dto.setPrecio(s.getPrecio());
        dto.setActivo(s.isActivo());

        List<OficioImagenSalidaDto> imgs = (s.getImagenes() == null) ? List.of()
                : s.getImagenes().stream()
                .map(i -> new OficioImagenSalidaDto(
                        i.getIdImage(),
                        i.getImageUrl(),
                        i.getNombreOriginal(),
                        i.getTipoImagen()
                ))
                .toList();

        dto.setImagenes(imgs);
        return dto;
    }
}
