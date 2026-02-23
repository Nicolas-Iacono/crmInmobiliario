package com.backend.crmInmobiliario.service.impl.aliados;

import com.backend.crmInmobiliario.DTO.entrada.aliados.ResenaCrearDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.ResenaSalidaDto;
import com.backend.crmInmobiliario.entity.OficioProveedor;
import com.backend.crmInmobiliario.entity.OficioResena;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.aliados.OficioProveedorRepository;
import com.backend.crmInmobiliario.repository.aliados.OficioResenaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OficioResenaService {

    private final OficioResenaRepository resenaRepo;
    private final OficioProveedorRepository proveedorRepo;
    private final UsuarioRepository usuarioRepo;

    public OficioResenaService(OficioResenaRepository resenaRepo,
                               OficioProveedorRepository proveedorRepo,
                               UsuarioRepository usuarioRepo) {
        this.resenaRepo = resenaRepo;
        this.proveedorRepo = proveedorRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public void crearOActualizar(Long userId, Long proveedorId, ResenaCrearDto dto) {

        validar(dto);

        Usuario usuario = usuarioRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // opcional: asegurar que sea inmobiliaria/admin, etc.
        // if (!usuario.tieneRolInmobiliaria()) ...

        OficioProveedor proveedor = proveedorRepo.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        // evitar auto-review: si el proveedor pertenece a ese usuario
        if (proveedor.getUsuario() != null && proveedor.getUsuario().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("No podés calificarte a vos mismo");
        }

        OficioResena resena = resenaRepo.findByProveedorIdAndUsuarioId(proveedorId, userId)
                .orElseGet(OficioResena::new);

        resena.setUsuario(usuario);
        resena.setProveedor(proveedor);
        resena.setCalificacion(dto.getCalificacion());
        resena.setComentario(dto.getComentario());

        resenaRepo.save(resena);

        recalcularMetricasProveedor(proveedorId);
    }

    @Transactional(readOnly = true)
    public List<ResenaSalidaDto> listarPorProveedor(Long proveedorId) {
        return resenaRepo.findAllByProveedorIdConUsuario(proveedorId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public void eliminarMiResena(Long userId, Long proveedorId) {
        OficioResena r = resenaRepo.findByProveedorIdAndUsuarioId(proveedorId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("No tenés reseña para este proveedor"));

        resenaRepo.delete(r);
        recalcularMetricasProveedor(proveedorId);
    }

    private void validar(ResenaCrearDto dto) {
        if (dto.getCalificacion() == null || dto.getCalificacion() < 1 || dto.getCalificacion() > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
        }
        if (dto.getComentario() != null && dto.getComentario().length() > 2000) {
            throw new IllegalArgumentException("Comentario demasiado largo");
        }
    }

    private void recalcularMetricasProveedor(Long proveedorId) {
        Double avg = resenaRepo.avgByProveedor(proveedorId);
        Long count = resenaRepo.countByProveedor(proveedorId);

        OficioProveedor proveedor = proveedorRepo.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        proveedor.setPromedioCalificacion(avg == null ? 0.0 : avg);
        proveedor.setTotalCalificaciones(count == null ? 0 : count.intValue());

        proveedorRepo.save(proveedor);
    }

    private ResenaSalidaDto toDto(OficioResena r) {
        ResenaSalidaDto dto = new ResenaSalidaDto();
        dto.setId(r.getId());
        dto.setCalificacion(r.getCalificacion());
        dto.setComentario(r.getComentario());
        dto.setFechaCreacion(r.getFechaCreacion());

        Usuario u = r.getUsuario();
        dto.setUsuarioId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setNombreNegocio(u.getNombreNegocio());

        return dto;
    }
}
