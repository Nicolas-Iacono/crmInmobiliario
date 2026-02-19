package com.backend.crmInmobiliario.service.impl.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioProveedorCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioProveedorUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.ImageUrls;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import com.backend.crmInmobiliario.entity.oficios.OficioProveedor;
import com.backend.crmInmobiliario.entity.oficios.OficioServicio;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.ImageUrlsRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioCalificacionRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioProveedorRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioServicioRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.service.oficios.IOficioProveedorService;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OficioProveedorService implements IOficioProveedorService {

    private static final int MESES_GRACIA = 2;

    private final OficioProveedorRepository proveedorRepository;
    private final OficioServicioRepository servicioRepository;
    private final OficioCalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImagenService imagenService;
    private final ImageUrlsRepository imageUrlsRepository;

    public OficioProveedorService(OficioProveedorRepository proveedorRepository,
                                  OficioServicioRepository servicioRepository,
                                  OficioCalificacionRepository calificacionRepository,
                                  UsuarioRepository usuarioRepository,
                                  RoleRepository roleRepository,
                                  PlanRepository planRepository,
                                  PasswordEncoder passwordEncoder,
                                  ImagenService imagenService, ImageUrlsRepository imageUrlsRepository) {
        this.proveedorRepository = proveedorRepository;
        this.servicioRepository = servicioRepository;
        this.calificacionRepository = calificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.planRepository = planRepository;
        this.passwordEncoder = passwordEncoder;
        this.imagenService = imagenService;
        this.imageUrlsRepository = imageUrlsRepository;
    }

    @Override
    public List<CategoriaOficio> listarCategorias() {
        return CategoriaOficio.catalogoBase();
    }

    @Transactional
    @Override
    public OficioProveedorSalidaDto registrarProveedor(OficioProveedorCreateDto dto) {

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameAlreadyExistsException("El username ya se encuentra registrado");
        }

        // 1) Crear Usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        // opcional: copiar algunos datos al usuario (si tu app los usa)
        usuario.setEmail(dto.getEmailContacto());
        usuario.setTelefono(dto.getTelefonoContacto());
        usuario.setLocalidad(dto.getLocalidad());
        usuario.setProvincia(dto.getProvincia());
        usuario.setNombreNegocio(dto.getEmpresa());

        // 2) Asignar rol OFICIO_ADMIN
        Role oficioRole = roleRepository.findByRol(RolesCostantes.OFICIO_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.OFICIO_ADMIN)));

        usuario.setRoles(Collections.singleton(oficioRole));

        Usuario userPersistido = usuarioRepository.save(usuario);

        // 3) Crear OficioProveedor ligado al usuario
        LocalDate hoy = LocalDate.now();

        OficioProveedor proveedor = new OficioProveedor();
        proveedor.setUsuario(userPersistido);
        proveedor.setNombreCompleto(dto.getNombreCompleto());
        proveedor.setEmpresa(dto.getEmpresa());
        proveedor.setEmailContacto(dto.getEmailContacto());
        proveedor.setTelefonoContacto(dto.getTelefonoContacto());
        proveedor.setDescripcion(dto.getDescripcion());
        proveedor.setLocalidad(dto.getLocalidad());
        proveedor.setProvincia(dto.getProvincia());
        proveedor.setFechaRegistro(hoy);
        proveedor.setPeriodoGraciaHasta(hoy.plusMonths(MESES_GRACIA));
        proveedor.setCategorias(dto.getCategorias() != null ? new ArrayList<>(dto.getCategorias()) : new ArrayList<>());

        // imagen perfil se sube por endpoint multipart separado
        proveedor.setImagenPerfil(null);

        OficioProveedor guardado = proveedorRepository.save(proveedor);
        return toDto(guardado);
    }
    // =========================================================
    // MI PERFIL
    // =========================================================

    @Override
    @Transactional
    public OficioProveedorSalidaDto obtenerMiPerfil(Long userId) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor"));
        return toDto(proveedor);
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto actualizarMiPerfil(Long userId, OficioProveedorUpdateDto dto) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor"));

        if (dto.getNombreCompleto() != null) proveedor.setNombreCompleto(dto.getNombreCompleto());
        if (dto.getEmpresa() != null) proveedor.setEmpresa(dto.getEmpresa());
        if (dto.getEmailContacto() != null) proveedor.setEmailContacto(dto.getEmailContacto());
        if (dto.getTelefonoContacto() != null) proveedor.setTelefonoContacto(dto.getTelefonoContacto());
        if (dto.getDescripcion() != null) proveedor.setDescripcion(dto.getDescripcion());
        if (dto.getLocalidad() != null) proveedor.setLocalidad(dto.getLocalidad());
        if (dto.getProvincia() != null) proveedor.setProvincia(dto.getProvincia());
        if (dto.getCategorias() != null) proveedor.setCategorias(new ArrayList<>(dto.getCategorias()));

        proveedorRepository.save(proveedor);
        return toDto(proveedor);
    }

    @Override
    @Transactional
    public void eliminarMiPerfil(Long userId) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor"));

        // Si querés soft delete, acá es el lugar
        proveedorRepository.delete(proveedor);
    }

    @Transactional
    @Override
    public List<OficioServicioSalidaDto> listarMisServicios(Long userId) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor"));

        return servicioRepository.findByProveedorId(proveedor.getId())
                .stream()
                .map(this::toServicioDto)
                .toList();
    }

    private OficioServicioSalidaDto toServicioDto(OficioServicio s) {
        OficioServicioSalidaDto dto = new OficioServicioSalidaDto();
        dto.setId(s.getId());
        dto.setTitulo(s.getTitulo());
        dto.setDescripcion(s.getDescripcion());
        dto.setPrecioDesdeArs(s.getPrecioDesdeArs());
        dto.setPrecio(s.getPrecio());
        dto.setImagenes(s.getImagenes());

        return dto;
    }
    // =========================================================
    // IMAGEN DE PERFIL
    // =========================================================



    @Override
    @Transactional
    public OficioProveedorSalidaDto actualizarImagenPerfil(Long userId, MultipartFile archivo) throws IOException {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor"));

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No se recibió ninguna imagen");
        }

        // 1) borrar anterior (storage + BD)
        ImageUrls actual = proveedor.getImagenPerfil();
        if (actual != null) {
            imagenService.eliminarDeStorageSupabase(actual.getImageUrl());
            proveedor.setImagenPerfil(null);
            imageUrlsRepository.delete(actual);
        }

        // 2) subir nueva
        byte[] webp = imagenService.convertirImagenExternamente(archivo);
        String nombreArchivo = "oficios/proveedor/" + proveedor.getId() + "-" + UUID.randomUUID() + ".webp";
        String url = imagenService.subirAStorageSupabase(webp, nombreArchivo);

        // 3) crear entidad ImageUrls
        ImageUrls nueva = new ImageUrls();
        nueva.setImageUrl(url);
        nueva.setNombreOriginal(archivo.getOriginalFilename());
        nueva.setTipoImagen("LOGO_EMPRESA");
        nueva.setFechaSubida(LocalDateTime.now());
        nueva.setProveedor(proveedor);

        ImageUrls guardada = imageUrlsRepository.save(nueva);
        proveedor.setImagenPerfil(guardada);

        proveedorRepository.save(proveedor);
        return toDto(proveedor);
    }
    // =========================================================
    // LISTADOS PÚBLICOS
    // =========================================================

    @Override
    @Transactional
    public List<OficioProveedorSalidaDto> listarProveedoresVisibles() {
        return proveedorRepository.findAll().stream()
                .filter(this::esVisibleEnListado)
                .map(this::toDto)
                .toList();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean esVisibleEnListado(OficioProveedor proveedor) {
        LocalDate hoy = LocalDate.now();

        boolean enPeriodoGracia = !hoy.isAfter(proveedor.getPeriodoGraciaHasta());
        if (enPeriodoGracia) return true;

        Plan plan = proveedor.getPlan();
        return plan != null && plan.isActive();
    }

    private OficioProveedorSalidaDto toDto(OficioProveedor p) {
        boolean enGracia = !LocalDate.now().isAfter(p.getPeriodoGraciaHasta());
        boolean planActivo = p.getPlan() != null && p.getPlan().isActive();

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
        dto.setImagenPerfilUrl(
                p.getImagenPerfil() != null
                        ? p.getImagenPerfil().getImageUrl()
                        : null
        );
        dto.setPromedioCalificacion(p.getPromedioCalificacion());
        dto.setTotalCalificaciones(p.getTotalCalificaciones());
        dto.setPlanId(p.getPlan() != null ? p.getPlan().getId() : null);
        dto.setPlanActivo(planActivo);

        return dto;
    }
}