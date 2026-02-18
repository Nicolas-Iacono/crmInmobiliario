package com.backend.crmInmobiliario.service.impl.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.RegistroOficioProveedorDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.Role;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import com.backend.crmInmobiliario.entity.oficios.OficioCalificacion;
import com.backend.crmInmobiliario.entity.oficios.OficioProveedor;
import com.backend.crmInmobiliario.entity.oficios.OficioServicio;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.exception.UsernameAlreadyExistsException;
import com.backend.crmInmobiliario.repository.USER_REPO.RoleRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioCalificacionRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioProveedorRepository;
import com.backend.crmInmobiliario.repository.oficios.OficioServicioRepository;
import com.backend.crmInmobiliario.service.oficios.IOficioProveedorService;
import com.backend.crmInmobiliario.utils.RolesCostantes;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OficioProveedorService implements IOficioProveedorService {

    private final OficioProveedorRepository proveedorRepository;
    private final OficioServicioRepository servicioRepository;
    private final OficioCalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public OficioProveedorService(OficioProveedorRepository proveedorRepository,
                                  OficioServicioRepository servicioRepository,
                                  OficioCalificacionRepository calificacionRepository,
                                  UsuarioRepository usuarioRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder) {
        this.proveedorRepository = proveedorRepository;
        this.servicioRepository = servicioRepository;
        this.calificacionRepository = calificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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

        OficioProveedor proveedor = new OficioProveedor();
        proveedor.setUsuario(userPersistido);
        proveedor.setNombreCompleto(dto.getNombreCompleto());
        proveedor.setEmpresa(dto.getEmpresa());
        proveedor.setEmailContacto(dto.getEmailContacto());
        proveedor.setTelefonoContacto(dto.getTelefonoContacto());
        proveedor.setDescripcion(dto.getDescripcion());
        proveedor.setLocalidad(dto.getLocalidad());
        proveedor.setProvincia(dto.getProvincia());
        proveedor.setCategorias(dto.getCategorias() != null ? dto.getCategorias() : new ArrayList<>());
        proveedor.setImagenesEmpresa(dto.getImagenesEmpresa() != null ? dto.getImagenesEmpresa() : new ArrayList<>());
        proveedor.setMontoSuscripcionMensualArs(dto.getMontoSuscripcionMensualArs());
        proveedor.setSuscripcionActiva(Boolean.FALSE);

        return toDto(proveedorRepository.save(proveedor));
    }

    @Override
    public List<OficioProveedorSalidaDto> listarProveedoresVisibles() {
        return proveedorRepository.findBySuscripcionActivaTrue()
                .stream()
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
        servicio.setImagenesTrabajos(dto.getImagenesTrabajos() != null ? dto.getImagenesTrabajos() : new ArrayList<>());
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
    public OficioProveedorSalidaDto actualizarSuscripcion(Long userId, boolean activa, LocalDate venceEl, BigDecimal montoMensualArs) {
        OficioProveedor proveedor = proveedorRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de proveedor de oficios"));

        proveedor.setSuscripcionActiva(activa);
        proveedor.setSuscripcionVenceEl(venceEl);
        proveedor.setMontoSuscripcionMensualArs(montoMensualArs);

        return toDto(proveedorRepository.save(proveedor));
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
                .suscripcionActiva(proveedor.getSuscripcionActiva())
                .suscripcionVenceEl(proveedor.getSuscripcionVenceEl())
                .montoSuscripcionMensualArs(proveedor.getMontoSuscripcionMensualArs())
                .servicios(servicios)
                .build();
    }
}
