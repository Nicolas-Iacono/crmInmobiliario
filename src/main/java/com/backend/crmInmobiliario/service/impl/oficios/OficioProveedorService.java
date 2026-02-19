package com.backend.crmInmobiliario.service.impl.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioProveedorCreateDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioProveedorUpdateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioServicioUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.ImageUrls;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import com.backend.crmInmobiliario.entity.oficios.OficioCalificacion;
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
                                  ImagenService imagenService,
                                  ImageUrlsRepository imageUrlsRepository) {
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

    @Override
    @Transactional
    public OficioProveedorSalidaDto registrarProveedor(OficioProveedorCreateDto dto) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameAlreadyExistsException("El username ya se encuentra registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEmail(dto.getEmailContacto());
        usuario.setTelefono(dto.getTelefonoContacto());
        usuario.setLocalidad(dto.getLocalidad());
        usuario.setProvincia(dto.getProvincia());
        usuario.setNombreNegocio(dto.getEmpresa());

        Role oficioRole = roleRepository.findByRol(RolesCostantes.OFICIO_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.OFICIO_ADMIN)));
        usuario.setRoles(Collections.singleton(oficioRole));
        Usuario userPersistido = usuarioRepository.save(usuario);

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
        proveedor.setImagenPerfil(null);

        return toProveedorDto(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto obtenerMiPerfil(Long userId) {
        return toProveedorDto(getProveedorByUserId(userId));
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto actualizarMiPerfil(Long userId, OficioProveedorUpdateDto dto) {
        OficioProveedor proveedor = getProveedorByUserId(userId);

        if (dto.getNombreCompleto() != null) proveedor.setNombreCompleto(dto.getNombreCompleto());
        if (dto.getEmpresa() != null) proveedor.setEmpresa(dto.getEmpresa());
        if (dto.getEmailContacto() != null) proveedor.setEmailContacto(dto.getEmailContacto());
        if (dto.getTelefonoContacto() != null) proveedor.setTelefonoContacto(dto.getTelefonoContacto());
        if (dto.getDescripcion() != null) proveedor.setDescripcion(dto.getDescripcion());
        if (dto.getLocalidad() != null) proveedor.setLocalidad(dto.getLocalidad());
        if (dto.getProvincia() != null) proveedor.setProvincia(dto.getProvincia());
        if (dto.getCategorias() != null) proveedor.setCategorias(new ArrayList<>(dto.getCategorias()));

        return toProveedorDto(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public void eliminarMiPerfil(Long userId) {
        OficioProveedor proveedor = getProveedorByUserId(userId);

        if (proveedor.getImagenPerfil() != null) {
            try {
                imagenService.eliminarDeStorageSupabase(proveedor.getImagenPerfil().getImageUrl());
            } catch (IOException e) {
                throw new RuntimeException("Error al eliminar imagen de perfil en storage", e);
            }
            imageUrlsRepository.delete(proveedor.getImagenPerfil());
        }

        proveedorRepository.delete(proveedor);
    }

    @Override
    @Transactional
    public List<OficioProveedorSalidaDto> listarProveedoresVisibles() {
        return proveedorRepository.findAll().stream()
                .filter(this::esVisibleEnListado)
                .map(this::toProveedorDto)
                .toList();
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto asignarPlan(Long userId, Long planId) {
        OficioProveedor proveedor = getProveedorByUserId(userId);
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el plan"));

        if (!plan.isActive()) {
            throw new IllegalArgumentException("El plan no está activo");
        }

        proveedor.setPlan(plan);
        return toProveedorDto(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto actualizarImagenPerfil(Long userId, MultipartFile archivo) throws IOException {
        OficioProveedor proveedor = getProveedorByUserId(userId);
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("No se recibió ninguna imagen");
        }

        ImageUrls actual = proveedor.getImagenPerfil();
        if (actual != null) {
            imagenService.eliminarDeStorageSupabase(actual.getImageUrl());
            proveedor.setImagenPerfil(null);
            imageUrlsRepository.delete(actual);
        }

        byte[] webp = imagenService.convertirImagenExternamente(archivo);
        String nombreArchivo = "oficios/proveedor/" + proveedor.getId() + "-" + UUID.randomUUID() + ".webp";
        String url = imagenService.subirAStorageSupabase(webp, nombreArchivo);

        ImageUrls nueva = new ImageUrls();
        nueva.setImageUrl(url);
        nueva.setNombreOriginal(archivo.getOriginalFilename());
        nueva.setTipoImagen("LOGO_EMPRESA");
        nueva.setFechaSubida(LocalDateTime.now());
        nueva.setProveedor(proveedor);

        ImageUrls guardada = imageUrlsRepository.save(nueva);
        proveedor.setImagenPerfil(guardada);

        return toProveedorDto(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public OficioServicioSalidaDto agregarServicio(Long userId, OficioServicioCreateDto dto, MultipartFile[] imagenes) throws IOException {
        OficioProveedor proveedor = getProveedorByUserId(userId);

        OficioServicio servicio = new OficioServicio();
        servicio.setProveedor(proveedor);
        servicio.setTitulo(dto.getTitulo());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setPrecioDesdeArs(dto.getPrecioDesdeArs());
        servicio.setPrecio(dto.getPrecio());
        servicio.setActivo(true);

        OficioServicio guardado = servicioRepository.save(servicio);
        subirImagenesServicio(guardado, imagenes);

        return toServicioDto(servicioRepository.save(guardado));
    }

    @Override
    @Transactional
    public List<OficioServicioSalidaDto> listarMisServicios(Long userId) {
        OficioProveedor proveedor = getProveedorByUserId(userId);
        return servicioRepository.findByProveedorId(proveedor.getId()).stream()
                .map(this::toServicioDto)
                .toList();
    }

    @Override
    @Transactional
    public OficioServicioSalidaDto editarServicio(Long userId, Long servicioId, OficioServicioUpdateDto dto, MultipartFile[] nuevasImagenes) throws IOException {
        OficioProveedor proveedor = getProveedorByUserId(userId);
        OficioServicio servicio = servicioRepository.findByIdAndProveedorId(servicioId, proveedor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el servicio del proveedor"));

        if (dto.getTitulo() != null) servicio.setTitulo(dto.getTitulo());
        if (dto.getDescripcion() != null) servicio.setDescripcion(dto.getDescripcion());
        if (dto.getPrecioDesdeArs() != null) servicio.setPrecioDesdeArs(dto.getPrecioDesdeArs());
        if (dto.getPrecio() != null) servicio.setPrecio(dto.getPrecio());
        if (dto.getActivo() != null) servicio.setActivo(dto.getActivo());

        if (Boolean.TRUE.equals(dto.getReplaceImages())) {
            borrarImagenesServicio(servicio);
        }

        subirImagenesServicio(servicio, nuevasImagenes);

        return toServicioDto(servicioRepository.save(servicio));
    }

    @Override
    @Transactional
    public void eliminarServicio(Long userId, Long servicioId) {
        OficioProveedor proveedor = getProveedorByUserId(userId);
        OficioServicio servicio = servicioRepository.findByIdAndProveedorId(servicioId, proveedor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el servicio del proveedor"));

        borrarImagenesServicio(servicio);
        servicioRepository.delete(servicio);
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto calificarProveedor(Long proveedorId, Long inmobiliariaId, OficioCalificacionEntradaDto dto) {
        OficioProveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el proveedor"));
        Usuario inmobiliaria = usuarioRepository.findById(inmobiliariaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la inmobiliaria"));

        OficioCalificacion calificacion = calificacionRepository
                .findByProveedorIdAndInmobiliariaId(proveedorId, inmobiliariaId)
                .orElseGet(OficioCalificacion::new);

        calificacion.setProveedor(proveedor);
        calificacion.setInmobiliaria(inmobiliaria);
        calificacion.setPuntaje(dto.getPuntaje());
        calificacion.setComentario(dto.getComentario());
        calificacionRepository.save(calificacion);

        List<OficioCalificacion> calificaciones = calificacionRepository.findByProveedorId(proveedorId);
        double promedio = calificaciones.stream().mapToInt(OficioCalificacion::getPuntaje).average().orElse(0.0);

        proveedor.setPromedioCalificacion(promedio);
        proveedor.setTotalCalificaciones(calificaciones.size());

        return toProveedorDto(proveedorRepository.save(proveedor));
    }

    private void subirImagenesServicio(OficioServicio servicio, MultipartFile[] imagenes) throws IOException {
        if (imagenes == null) {
            return;
        }

        for (MultipartFile archivo : imagenes) {
            if (archivo == null || archivo.isEmpty()) {
                continue;
            }

            byte[] webp = imagenService.convertirImagenExternamente(archivo);
            String nombreArchivo = "oficios/servicios/" + servicio.getId() + "-" + UUID.randomUUID() + ".webp";
            String url = imagenService.subirAStorageSupabase(webp, nombreArchivo);

            ImageUrls image = new ImageUrls();
            image.setImageUrl(url);
            image.setNombreOriginal(archivo.getOriginalFilename());
            image.setTipoImagen("SERVICIO_TRABAJO");
            image.setFechaSubida(LocalDateTime.now());
            image.setServicio(servicio);

            ImageUrls saved = imageUrlsRepository.save(image);
            servicio.getImagenes().add(saved);
        }
    }

    private void borrarImagenesServicio(OficioServicio servicio) {
        List<ImageUrls> actuales = new ArrayList<>(servicio.getImagenes());
        for (ImageUrls image : actuales) {
            try {
                imagenService.eliminarDeStorageSupabase(image.getImageUrl());
            } catch (IOException e) {
                throw new RuntimeException("Error al eliminar imagen de servicio en storage", e);
            }
            servicio.getImagenes().remove(image);
            imageUrlsRepository.delete(image);
        }
    }

    private boolean esVisibleEnListado(OficioProveedor proveedor) {
        LocalDate hoy = LocalDate.now();
        boolean enPeriodoGracia = !hoy.isAfter(proveedor.getPeriodoGraciaHasta());
        if (enPeriodoGracia) {
            return true;
        }

        Plan plan = proveedor.getPlan();
        return plan != null && plan.isActive();
    }

    private OficioProveedor getProveedorByUserId(Long userId) {
        return proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor"));
    }

    private OficioProveedorSalidaDto toProveedorDto(OficioProveedor proveedor) {
        OficioProveedorSalidaDto dto = new OficioProveedorSalidaDto();
        dto.setId(proveedor.getId());
        dto.setNombreCompleto(proveedor.getNombreCompleto());
        dto.setEmpresa(proveedor.getEmpresa());
        dto.setEmailContacto(proveedor.getEmailContacto());
        dto.setTelefonoContacto(proveedor.getTelefonoContacto());
        dto.setDescripcion(proveedor.getDescripcion());
        dto.setLocalidad(proveedor.getLocalidad());
        dto.setProvincia(proveedor.getProvincia());
        dto.setCategorias(proveedor.getCategorias() != null ? new ArrayList<>(proveedor.getCategorias()) : List.of());
        dto.setImagenPerfilUrl(proveedor.getImagenPerfil() != null ? proveedor.getImagenPerfil().getImageUrl() : null);
        dto.setPromedioCalificacion(proveedor.getPromedioCalificacion());
        dto.setTotalCalificaciones(proveedor.getTotalCalificaciones());
        dto.setPlanId(proveedor.getPlan() != null ? proveedor.getPlan().getId() : null);
        dto.setPlanActivo(proveedor.getPlan() != null && proveedor.getPlan().isActive());
        return dto;
    }

    private OficioServicioSalidaDto toServicioDto(OficioServicio servicio) {
        OficioServicioSalidaDto dto = new OficioServicioSalidaDto();
        dto.setId(servicio.getId());
        dto.setTitulo(servicio.getTitulo());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setPrecioDesdeArs(servicio.getPrecioDesdeArs());
        dto.setPrecio(servicio.getPrecio());
        dto.setActivo(servicio.isActivo());
        dto.setImagenes(servicio.getImagenes().stream().map(ImageUrls::getImageUrl).toList());
        return dto;
    }
}
