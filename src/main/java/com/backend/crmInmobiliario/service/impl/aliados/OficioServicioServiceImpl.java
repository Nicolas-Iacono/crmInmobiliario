package com.backend.crmInmobiliario.service.impl.aliados;

import com.backend.crmInmobiliario.DTO.entrada.aliados.OficioProveedorCreateDto;
import com.backend.crmInmobiliario.DTO.entrada.aliados.OficioServicioCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.aliados.OficioServicioUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioImagenSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.ImageUrlsRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.aliados.OficioProveedorRepository;
import com.backend.crmInmobiliario.repository.aliados.OficioServicioRepository;
import com.backend.crmInmobiliario.service.IOficioServicioService;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OficioServicioServiceImpl implements IOficioServicioService {

    private final OficioProveedorRepository proveedorRepository;
    private final OficioServicioRepository servicioRepository;
    private final OficioImagenService oficioImagenService; // tu servicio externo (supabase/webp)
    private final ImageUrlsRepository imageUrlsRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Transactional
    @Override
    public OficioProveedorSalidaDto registrarProveedor(OficioProveedorCreateDto dto) {

        // 1️⃣ validar username
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameAlreadyExistsException("El username ya existe");
        }

        // 2️⃣ crear usuario (AUTH)
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEmail(dto.getEmailContacto());
        usuario.setTelefono(dto.getTelefonoContacto());
        usuario.setLocalidad(dto.getLocalidad());
        usuario.setProvincia(dto.getProvincia());

        // 3️⃣ asignar rol OFICIO_ADMIN
        Role rol = roleRepository.findByRol(RolesCostantes.OFICIO_ADMIN)
                .orElseThrow(() -> new RuntimeException("Rol OFICIO_ADMIN no existe"));

        usuario.getRoles().add(rol);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 4️⃣ crear proveedor
        OficioProveedor proveedor = new OficioProveedor();
        proveedor.setUsuario(usuarioGuardado);
        proveedor.setNombreCompleto(dto.getNombreCompleto());
        proveedor.setEmpresa(dto.getEmpresa());
        proveedor.setEmailContacto(dto.getEmailContacto());
        proveedor.setTelefonoContacto(dto.getTelefonoContacto());
        proveedor.setDescripcion(dto.getDescripcion());
        proveedor.setLocalidad(dto.getLocalidad());
        proveedor.setProvincia(dto.getProvincia());
        proveedor.setCategorias(
                dto.getCategorias() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(dto.getCategorias())
        );

        proveedor.setFechaRegistro(LocalDate.now());
        OficioProveedor guardado = proveedorRepository.save(proveedor);

        return toProveedorDto(guardado);
    }

    private OficioProveedorSalidaDto toProveedorDto(OficioProveedor p) {
        OficioProveedorSalidaDto dto = new OficioProveedorSalidaDto();

        dto.setId(p.getId());
        dto.setNombreCompleto(p.getNombreCompleto());
        dto.setEmpresa(p.getEmpresa());
        dto.setEmailContacto(p.getEmailContacto());
        dto.setTelefonoContacto(p.getTelefonoContacto());
        dto.setDescripcion(p.getDescripcion());
        dto.setLocalidad(p.getLocalidad());
        dto.setProvincia(p.getProvincia());

        dto.setCategorias(p.getCategorias() != null ? new ArrayList<>(p.getCategorias()) : List.of());

        dto.setPromedioCalificacion(p.getPromedioCalificacion() != null ? p.getPromedioCalificacion() : 0.0);
        dto.setTotalCalificaciones(p.getTotalCalificaciones() != null ? p.getTotalCalificaciones() : 0);

        // Opcional: si tenés imagen de perfil/empresa
        if (p.getImagenPerfil() != null) {
            dto.setImagenPerfilUrl(p.getImagenPerfil().getImageUrl());
        } else {
            dto.setImagenPerfilUrl(null);
        }

        return dto;
    }


    @Override
    @Transactional
    public OficioServicioSalidaDto crear(Long userId, OficioServicioCreateDto dto, MultipartFile[] imagenes) {
        OficioProveedor proveedor = getProveedorByUserId(userId);

        OficioServicio servicio = new OficioServicio();
        servicio.setProveedor(proveedor);
        servicio.setTitulo(dto.getTitulo());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setPrecio(dto.getPrecio());
        servicio.setActivo(true);

        OficioServicio guardado = servicioRepository.save(servicio);

        // Imágenes: best-effort (no rompas alta)
        if (imagenes != null && imagenes.length > 0) {
            try {
                oficioImagenService.subirImagenesServicio(guardado.getId(), imagenes);
            } catch (Exception e) {
                // logger.warn("Servicio creado pero falló subida de imágenes. servicioId={}", guardado.getId(), e);
            }
        }

        return toServicioDto(refrescar(guardado.getId()));
    }

    @Override
    @Transactional
    public List<OficioServicioSalidaDto> listarMisServicios(Long userId) {
        OficioProveedor proveedor = getProveedorByUserId(userId);

        return servicioRepository.findByProveedorId(proveedor.getId())
                .stream()
                .map(this::toServicioDto)
                .toList();
    }

    @Override
    @Transactional
    public OficioServicioSalidaDto obtenerMiServicio(Long userId, Long servicioId) {
        OficioProveedor proveedor = getProveedorByUserId(userId);

        OficioServicio servicio = servicioRepository.findByIdAndProveedorId(servicioId, proveedor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        return toServicioDto(servicio);
    }

    @Override
    @Transactional
    public OficioServicioSalidaDto editar(Long userId, Long servicioId, OficioServicioUpdateDto dto, MultipartFile[] imagenes) {
        OficioProveedor proveedor = getProveedorByUserId(userId);

        OficioServicio servicio = servicioRepository.findByIdAndProveedorId(servicioId, proveedor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (dto.getTitulo() != null) servicio.setTitulo(dto.getTitulo());
        if (dto.getDescripcion() != null) servicio.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) servicio.setPrecio(dto.getPrecio());
        if (dto.getActivo() != null) servicio.setActivo(dto.getActivo());

        servicioRepository.save(servicio);

        // Imágenes: best-effort (no rompas edición)
        if (imagenes != null && imagenes.length > 0) {
            try {
                // decisión: agrego nuevas (no borro) — o podés hacer replace
                oficioImagenService.subirImagenesServicio(servicio.getId(), imagenes);
            } catch (Exception e) {
                // logger.warn("Servicio editado pero falló subida de imágenes. servicioId={}", servicio.getId(), e);
            }
        }

        return toServicioDto(refrescar(servicio.getId()));
    }

    @Override
    @Transactional
    public void eliminar(Long userId, Long servicioId) {
        OficioProveedor proveedor = getProveedorByUserId(userId);

        OficioServicio servicio = servicioRepository.findByIdAndProveedorId(servicioId, proveedor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // Borro imágenes (DB + storage) antes de borrar servicio
        try {
            oficioImagenService.eliminarImagenesDeServicio(servicio.getId());
        } catch (Exception e) {
            // si querés ser estricto, tirá excepción; si no, log y seguí.
        }

        servicioRepository.delete(servicio);
    }

    // ----------------- helpers -----------------

    private OficioProveedor getProveedorByUserId(Long userId) {
        return proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor"));
    }

    private OficioServicio refrescar(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));
    }

    private OficioServicioSalidaDto toServicioDto(OficioServicio s) {
        OficioServicioSalidaDto dto = new OficioServicioSalidaDto();
        dto.setId(s.getId());
        dto.setTitulo(s.getTitulo());
        dto.setDescripcion(s.getDescripcion());
        dto.setPrecio(s.getPrecio());
        dto.setActivo(s.isActivo());

        List<OficioImagenSalidaDto> imagenes =
                (s.getImagenes() == null) ? List.of()
                        : s.getImagenes().stream()
                        .filter(Objects::nonNull)
                        .map(img -> {
                            OficioImagenSalidaDto i = new OficioImagenSalidaDto();
                            i.setIdImage(img.getIdImage());
                            i.setImageUrl(img.getImageUrl());
                            i.setTipoImagen(img.getTipoImagen());
                            return i;
                        })
                        .toList();

        dto.setImagenes(imagenes);
        return dto;
    }
}

