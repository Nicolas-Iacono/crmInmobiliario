package com.backend.crmInmobiliario.service.impl.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.RegistroOficioProveedorDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import com.backend.crmInmobiliario.entity.oficios.OficioCalificacion;
import com.backend.crmInmobiliario.entity.oficios.OficioProveedor;
import com.backend.crmInmobiliario.entity.oficios.OficioServicio;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioCalificacionRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioProveedorRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioServicioRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import com.backend.crmInmobiliario.service.oficios.IOficioProveedorService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public OficioProveedorService(OficioProveedorRepository proveedorRepository,
                                  OficioServicioRepository servicioRepository,
                                  OficioCalificacionRepository calificacionRepository,
                                  UsuarioRepository usuarioRepository,
                                  RoleRepository roleRepository,
                                  PlanRepository planRepository,
                                  PasswordEncoder passwordEncoder,
                                  ImagenService imagenService) {
        this.proveedorRepository = proveedorRepository;
        this.servicioRepository = servicioRepository;
        this.calificacionRepository = calificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.planRepository = planRepository;
        this.passwordEncoder = passwordEncoder;
        this.imagenService = imagenService;
    }

    @Override
    public List<CategoriaOficio> listarCategorias() {
        return CategoriaOficio.catalogoBase();
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto registrarProveedor(RegistroOficioProveedorDto dto) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new UsernameAlreadyExistsException("El username ya se encuentra registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombreNegocio(dto.getEmpresa());
        usuario.setEmail(dto.getEmailContacto());
        usuario.setTelefono(dto.getTelefonoContacto());
        usuario.setLocalidad(dto.getLocalidad());
        usuario.setProvincia(dto.getProvincia());

        Role oficioRole = roleRepository.findByRol(RolesCostantes.OFICIO_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RolesCostantes.OFICIO_ADMIN)));
        usuario.setRoles(Collections.singleton(oficioRole));
        Usuario userPersistido = usuarioRepository.save(usuario);

        LocalDate fechaRegistro = LocalDate.now();

        OficioProveedor proveedor = new OficioProveedor();
        proveedor.setUsuario(userPersistido);
        proveedor.setNombreCompleto(dto.getNombreCompleto());
        proveedor.setEmpresa(dto.getEmpresa());
        proveedor.setEmailContacto(dto.getEmailContacto());
        proveedor.setTelefonoContacto(dto.getTelefonoContacto());
        proveedor.setDescripcion(dto.getDescripcion());
        proveedor.setLocalidad(dto.getLocalidad());
        proveedor.setProvincia(dto.getProvincia());
        proveedor.setFechaRegistro(fechaRegistro);
        proveedor.setPeriodoGraciaHasta(fechaRegistro.plusMonths(MESES_GRACIA));
        proveedor.setCategorias(dto.getCategorias() != null ? dto.getCategorias() : new ArrayList<>());
        proveedor.setImagenesEmpresa(dto.getImagenesEmpresa() != null ? dto.getImagenesEmpresa() : new ArrayList<>());

        return toDto(proveedorRepository.save(proveedor));
    }

    @Override
    public List<OficioProveedorSalidaDto> listarProveedoresVisibles() {
        return proveedorRepository.findAll().stream()
                .filter(this::esVisibleEnListado)
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto agregarServicio(Long userId, OficioServicioEntradaDto dto) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor de oficios"));

        OficioServicio servicio = new OficioServicio();
        servicio.setProveedor(proveedor);
        servicio.setTitulo(dto.getTitulo());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setPrecioDesdeArs(dto.getPrecioDesdeArs());
        servicio.setPrecioHastaArs(dto.getPrecioHastaArs());
        servicioRepository.save(servicio);

        return toDto(proveedor);
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto calificarProveedor(Long proveedorId, Long inmobiliariaId, OficioCalificacionEntradaDto dto) {
        OficioProveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        Usuario inmobiliaria = usuarioRepository.findById(inmobiliariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Inmobiliaria no encontrada"));

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

        proveedor.setTotalCalificaciones(calificaciones.size());
        proveedor.setPromedioCalificacion(promedio);
        proveedorRepository.save(proveedor);

        return toDto(proveedor);
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto asignarPlan(Long userId, Long planId) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor de oficios"));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado"));

        if (!plan.isActive()) {
            throw new IllegalArgumentException("El plan seleccionado no está activo");
        }

        proveedor.setPlan(plan);
        return toDto(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public ImgUrlSalidaDto subirLogoProveedor(Long userId, MultipartFile archivo) {
        try {
            return imagenService.subirLogo(userId, archivo);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo subir el logo del proveedor", e);
        }
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto subirImagenesEmpresa(Long userId, MultipartFile[] archivos) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor de oficios"));

        List<String> urls;
        try {
            urls = imagenService.subirImagenesGenericas(archivos, "oficios/empresa");
        } catch (IOException e) {
            throw new RuntimeException("No se pudieron subir imágenes de empresa", e);
        }

        proveedor.getImagenesEmpresa().addAll(urls);
        return toDto(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public OficioProveedorSalidaDto subirImagenesTrabajo(Long userId, Long servicioId, MultipartFile[] archivos) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor de oficios"));

        OficioServicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (!servicio.getProveedor().getId().equals(proveedor.getId())) {
            throw new RuntimeException("No tenés permisos para subir imágenes a este servicio");
        }

        List<String> urls;
        try {
            urls = imagenService.subirImagenesGenericas(archivos, "oficios/trabajos");
        } catch (IOException e) {
            throw new RuntimeException("No se pudieron subir imágenes de trabajos", e);
        }

        servicio.getImagenesTrabajos().addAll(urls);
        servicioRepository.save(servicio);

        return toDto(proveedor);
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

    private OficioProveedorSalidaDto toDto(OficioProveedor proveedor) {
        List<OficioServicioSalidaDto> servicios = servicioRepository.findByProveedorId(proveedor.getId())
                .stream()
                .map(s -> OficioServicioSalidaDto.builder()
                        .id(s.getId())
                        .titulo(s.getTitulo())
                        .descripcion(s.getDescripcion())
                        .precioDesdeArs(s.getPrecioDesdeArs())
                        .precioHastaArs(s.getPrecioHastaArs())
                        .imagenesTrabajos(s.getImagenesTrabajos())
                        .build())
                .toList();

        LocalDate hoy = LocalDate.now();
        boolean enPeriodoGracia = !hoy.isAfter(proveedor.getPeriodoGraciaHasta());
        boolean planActivo = proveedor.getPlan() != null && proveedor.getPlan().isActive();

        return OficioProveedorSalidaDto.builder()
                .id(proveedor.getId())
                .nombreCompleto(proveedor.getNombreCompleto())
                .empresa(proveedor.getEmpresa())
                .emailContacto(proveedor.getEmailContacto())
                .telefonoContacto(proveedor.getTelefonoContacto())
                .descripcion(proveedor.getDescripcion())
                .localidad(proveedor.getLocalidad())
                .provincia(proveedor.getProvincia())
                .categorias(proveedor.getCategorias())
                .imagenesEmpresa(proveedor.getImagenesEmpresa())
                .promedioCalificacion(proveedor.getPromedioCalificacion())
                .totalCalificaciones(proveedor.getTotalCalificaciones())
                .fechaRegistro(proveedor.getFechaRegistro())
                .periodoGraciaHasta(proveedor.getPeriodoGraciaHasta())
                .enPeriodoGracia(enPeriodoGracia)
                .planId(proveedor.getPlan() != null ? proveedor.getPlan().getId() : null)
                .planCode(proveedor.getPlan() != null ? proveedor.getPlan().getCode() : null)
                .planNombre(proveedor.getPlan() != null ? proveedor.getPlan().getName() : null)
                .planActivo(planActivo)
                .visibleEnListado(enPeriodoGracia || planActivo)
                .servicios(servicios)
                .build();
    }
}
