package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.prospecto.ProspectoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ProspectoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.prospecto.ProspectoSalidaDto;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Prospecto;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.ProspectoRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import com.backend.crmInmobiliario.service.IProspectoService;
import com.backend.crmInmobiliario.service.impl.notificacionesPush.PushNotificationService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class ProspectoService implements IProspectoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProspectoService.class);

    @Autowired
    private ProspectoRepository prospectoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PropiedadRepository propiedadRepository;
    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;
    @Autowired
    private PushNotificationService pushNotificationService;
    @Autowired
    private ModelMapper modelMapper;
    private AuthUtil authUtil;

    public ProspectoService(ProspectoRepository prospectoRepository, UsuarioRepository usuarioRepository, PropiedadRepository propiedadRepository, PushSubscriptionRepository pushSubscriptionRepository, PushNotificationService pushNotificationService, ModelMapper modelMapper, AuthUtil authUtil) {
        this.prospectoRepository = prospectoRepository;
        this.usuarioRepository = usuarioRepository;
        this.propiedadRepository = propiedadRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.pushNotificationService = pushNotificationService;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
        configureMapping();
    }
    private void configureMapping() {

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true);


        modelMapper.typeMap(ProspectoEntradaDto.class, Prospecto.class)
                .addMapping(ProspectoEntradaDto::getNombre, Prospecto::setNombre)
                .addMapping(ProspectoEntradaDto::getApellido, Prospecto::setApellido)
                .addMapping(ProspectoEntradaDto::getTelefono, Prospecto::setTelefono)
                .addMapping(ProspectoEntradaDto::getZonaPreferencia, Prospecto::setZonaPreferencia)
                .addMapping(ProspectoEntradaDto::getCantidadAmbientes, Prospecto::setCantidadAmbientes)
                .addMapping(ProspectoEntradaDto::getCantidadPersonas, Prospecto::setCantidadPersonas)
                .addMapping(ProspectoEntradaDto::getRangoPrecioMin, Prospecto::setRangoPrecioMin)
                .addMapping(ProspectoEntradaDto::getRangoPrecioMax, Prospecto::setRangoPrecioMax)
                .addMapping(ProspectoEntradaDto::getCochera, Prospecto::setCochera)
                .addMapping(ProspectoEntradaDto::getPatio, Prospecto::setPatio)
                .addMapping(ProspectoEntradaDto::getJardin, Prospecto::setJardin)
                .addMapping(ProspectoEntradaDto::getPileta, Prospecto::setPileta)
                .addMapping(ProspectoEntradaDto::getVisibilidadPublico, Prospecto::setVisibilidadPublico)
                .addMapping(ProspectoEntradaDto::getDestino, Prospecto::setDestino)
                .addMapping(ProspectoEntradaDto::getMascotas, Prospecto::setMascotas)
                .addMapping(ProspectoEntradaDto::getDisponible, Prospecto::setDisponible);

        modelMapper.typeMap(Prospecto.class,ProspectoSalidaDto.class)
                .addMapping(Prospecto::getNombre, ProspectoSalidaDto::setNombre)
                .addMapping(Prospecto::getApellido, ProspectoSalidaDto::setApellido)
                .addMapping(Prospecto::getTelefono, ProspectoSalidaDto::setTelefono)
                .addMapping(Prospecto::getZonaPreferencia, ProspectoSalidaDto::setZonaPreferencia)
                .addMapping(Prospecto::getCantidadPersonas, ProspectoSalidaDto::setCantidadPersonas)
                .addMapping(Prospecto::getCantidadAmbientes, ProspectoSalidaDto::setCantidadAmbientes)
                .addMapping(Prospecto::getRangoPrecioMin, ProspectoSalidaDto::setRangoPrecioMin)
                .addMapping(Prospecto::getRangoPrecioMax, ProspectoSalidaDto::setRangoPrecioMax)
                .addMapping(Prospecto::getCochera, ProspectoSalidaDto::setCochera)
                .addMapping(Prospecto::getPatio, ProspectoSalidaDto::setPatio)
                .addMapping(Prospecto::getJardin, ProspectoSalidaDto::setJardin)
                .addMapping(Prospecto::getVisibilidadPublico, ProspectoSalidaDto::setVisibilidadPublico)
                .addMapping(Prospecto::getDestino, ProspectoSalidaDto::setDestino)
                .addMapping(Prospecto::getMascotas, ProspectoSalidaDto::setMascotas)
                .addMapping(Prospecto::getDisponible, ProspectoSalidaDto::setDisponible);

    }
        @Override
    @Transactional
    public ProspectoSalidaDto crearProspecto(ProspectoEntradaDto dto) throws ResourceNotFoundException {
        Long idUser = authUtil.extractUserId();
        Usuario usuario = usuarioRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));


        Prospecto prospecto = new Prospecto();
        prospecto.setUsuario(usuario);
        prospecto.setNombre(dto.getNombre());
        prospecto.setApellido(dto.getApellido());
        prospecto.setTelefono(dto.getTelefono());
        prospecto.setEmail(dto.getEmail());
        prospecto.setRangoPrecioMin(dto.getRangoPrecioMin());
        prospecto.setRangoPrecioMax(dto.getRangoPrecioMax());
        prospecto.setCantidadPersonas(dto.getCantidadPersonas());
        prospecto.setZonaPreferencia(dto.getZonaPreferencia());
        prospecto.setCantidadAmbientes(dto.getCantidadAmbientes());
        prospecto.setCochera(dto.getCochera());
        prospecto.setPatio(dto.getPatio());
        prospecto.setJardin(dto.getJardin());
        prospecto.setPileta(dto.getPileta());
        prospecto.setVisibilidadPublico(dto.getVisibilidadPublico() != null ? dto.getVisibilidadPublico() : Boolean.TRUE);
        prospecto.setDestino(dto.getDestino());
        prospecto.setMascotas(dto.getMascotas());
        prospecto.setDisponible(Boolean.TRUE);

        Prospecto guardado = prospectoRepository.save(prospecto);
        return mapSalida(guardado);
    }

    @Override
    @Transactional
    public ProspectoSalidaDto actualizarProspecto(Long id, ProspectoModificacionDto dto) throws ResourceNotFoundException {
        Long idUser = authUtil.extractUserId();

        // 1) Buscar el prospecto
        Prospecto prospecto = prospectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));

        // 2) Seguridad: validar que sea del usuario logueado
        if (prospecto.getUsuario() == null || prospecto.getUsuario().getId() == null
                || !prospecto.getUsuario().getId().equals(idUser)) {
            // Podés usar AccessDeniedException si preferís 403
            throw new ResourceNotFoundException("Prospecto no encontrado");
            // (Esto evita filtrar que existe pero es de otro usuario)
        }

        // 3) Aplicar cambios (parcial)
        boolean cambioFiltros = false;

        if (dto.getNombre() != null) prospecto.setNombre(dto.getNombre());
        if (dto.getApellido() != null) prospecto.setApellido(dto.getApellido());
        if (dto.getTelefono() != null) prospecto.setTelefono(dto.getTelefono());
        if (dto.getEmail() != null) prospecto.setEmail(dto.getEmail());

        if (dto.getRangoPrecioMin() != null) { prospecto.setRangoPrecioMin(dto.getRangoPrecioMin()); cambioFiltros = true; }
        if (dto.getRangoPrecioMax() != null) { prospecto.setRangoPrecioMax(dto.getRangoPrecioMax()); cambioFiltros = true; }
        if (dto.getCantidadPersonas() != null) { prospecto.setCantidadPersonas(dto.getCantidadPersonas()); cambioFiltros = true; }
        if (dto.getZonaPreferencia() != null) { prospecto.setZonaPreferencia(dto.getZonaPreferencia()); cambioFiltros = true; }
        if (dto.getCantidadAmbientes() != null) { prospecto.setCantidadAmbientes(dto.getCantidadAmbientes()); cambioFiltros = true; }
        if (dto.getCochera() != null) { prospecto.setCochera(dto.getCochera()); cambioFiltros = true; }
        if (dto.getPatio() != null) { prospecto.setPatio(dto.getPatio()); cambioFiltros = true; }
        if (dto.getJardin() != null) { prospecto.setJardin(dto.getJardin()); cambioFiltros = true; }
        if (dto.getPileta() != null) { prospecto.setPileta(dto.getPileta()); cambioFiltros = true; }
        if (dto.getVisibilidadPublico() != null) { prospecto.setVisibilidadPublico(dto.getVisibilidadPublico()); cambioFiltros = true; }
        if (dto.getDestino() != null) { prospecto.setDestino(dto.getDestino()); cambioFiltros = true; }
        if (dto.getMascotas() != null) { prospecto.setMascotas(dto.getMascotas()); cambioFiltros = true; }
        if (dto.getDisponible() != null) { prospecto.setDisponible(dto.getDisponible()); }

        // 4) Guardar
        Prospecto actualizado = prospectoRepository.save(prospecto);


        return mapSalida(actualizado);
    }

    @Override
    @Transactional
    public ProspectoSalidaDto actualizarDisponibilidad(Long id, Boolean disponible) throws ResourceNotFoundException {
        if (disponible == null) {
            throw new IllegalArgumentException("La disponibilidad es obligatoria");
        }

        Long idUser = authUtil.extractUserId();
        Prospecto prospecto = prospectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));

        validarPropietario(idUser, prospecto);

        prospecto.setDisponible(disponible);
        Prospecto actualizado = prospectoRepository.save(prospecto);
        return mapSalida(actualizado);
    }

    @Override
    @Transactional
    public List<ProspectoSalidaDto> listarMisProspectos() {
        Long userId = authUtil.extractUserId();

        return prospectoRepository.findByUsuarioId(userId).stream()
                .map(this::mapSalida)
                .toList();
    }
    @Override
    @Transactional
    public void eliminarProspecto(Long id) throws ResourceNotFoundException {
        Long userId = authUtil.extractUserId();

        Prospecto prospecto = prospectoRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));

        prospectoRepository.delete(prospecto);
    }

    private ProspectoSalidaDto mapSalida(Prospecto prospecto) {
        ProspectoSalidaDto dto = modelMapper.map(prospecto, ProspectoSalidaDto.class);
        if (prospecto.getUsuario() != null) {
            Usuario usuario = prospecto.getUsuario();
            dto.setNombreNegocio(usuario.getNombreNegocio());
            dto.setTelefonoUsuario(usuario.getTelefono());
            dto.setLogo(usuario.getLogoInmobiliaria() != null ? usuario.getLogoInmobiliaria().getImageUrl() : null);
        }
        return dto;
    }

    public void notificarProspectosCompatiblesPorPropiedad(Propiedad propiedad) {
        if (propiedad == null || propiedad.getUsuario() == null || propiedad.getUsuario().getId() == null) return;

        Long ownerId = propiedad.getUsuario().getId();

        // Prospectos de otros
        List<Prospecto> prospectos = prospectoRepository.findByUsuarioIdNot(ownerId);

        long matches = prospectos.stream()
                .filter(p -> Boolean.TRUE.equals(p.getVisibilidadPublico()))
                .filter(p -> p.cumpleConPropiedad(propiedad))
                .count();

        if (matches <= 0) return;

        // Push SOLO al dueño de la propiedad, avisándole que tiene matches
        pushSubscriptionRepository.findByUserId(ownerId).forEach(sub -> {
            try {
                pushNotificationService.enviarNotificacion(
                        sub,
                        "🎯 Prospectos compatibles",
                        "Tenés " + matches + " prospecto(s) que matchean con esta propiedad."
                );
            } catch (Exception e) {
                LOGGER.error("Error enviando push prospectos compatibles. ownerId={}, propiedadId={}", ownerId, propiedad.getId_propiedad(), e);
            }
        });
    }

    @Transactional
    @Override
    public List<ProspectoSalidaDto> listarProspectosCompatibles(Long propiedadId, Long userId) {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        // Seguridad: la propiedad debe ser del usuario logueado
        if (propiedad.getUsuario() == null || propiedad.getUsuario().getId() == null
                || !propiedad.getUsuario().getId().equals(userId)) {
            throw new ResourceNotFoundException("Propiedad no encontrada");
        }

        // Buscar prospectos de otros usuarios (marketplace)
        List<Prospecto> prospectos = prospectoRepository.findByUsuarioIdNot(userId);

        return prospectos.stream()
                .filter(p -> Boolean.TRUE.equals(p.getVisibilidadPublico()))
                .filter(p -> p.cumpleConPropiedad(propiedad))
                .map(this::mapProspectoSalida) // o modelMapper directo
                .toList();
    }

    private ProspectoSalidaDto mapProspectoSalida(Prospecto prospecto) {
        return mapSalida(prospecto);
    }

    @Override
    @Transactional
    public List<PropiedadSalidaDto> listarPropiedadesCompatibles(Long usuarioId, Long prospectoId)
            throws ResourceNotFoundException {

        Prospecto prospecto = prospectoRepository.findById(prospectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));
        validarPropietario(usuarioId, prospecto);

        // ✅ propias: disponibles (sin importar visibleAOtros)
        List<Propiedad> propias = propiedadRepository.findByUsuarioIdAndDisponibilidadTrue(usuarioId);

        // ✅ ajenas: visibles + disponibles
        List<Propiedad> ajenasVisibles = propiedadRepository
                .findByUsuarioIdNotAndVisibleAOtrosTrueAndDisponibilidadTrue(usuarioId);

        LOGGER.info("Compatibles: usuarioId={}, prospectoId={}", usuarioId, prospectoId);
        LOGGER.info("Propias disponibles: {}", propias.size());
        LOGGER.info("Ajenas visibles disponibles: {}", ajenasVisibles.size());

        long propiasOk = propias.stream().filter(prospecto::cumpleConPropiedad).count();
        long ajenasOk = ajenasVisibles.stream().filter(prospecto::cumpleConPropiedad).count();

        LOGGER.info("Luego de cumpleConPropiedad -> propiasOk={}, ajenasOk={}", propiasOk, ajenasOk);

        return Stream.concat(propias.stream(), ajenasVisibles.stream())
                .filter(prospecto::cumpleConPropiedad)
                .map(p -> {
                    PropiedadSalidaDto dto = modelMapper.map(p, PropiedadSalidaDto.class);
                    dto.setPropia(p.getUsuario() != null && p.getUsuario().getId().equals(usuarioId));
                    return dto;
                })
                .toList();
    }
    @Override
    @Transactional
    public void notificarPropiedadCompatible(Long usuarioId, Long prospectoId, Long propiedadId) throws ResourceNotFoundException {
        Prospecto prospecto = prospectoRepository.findById(prospectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));
        validarPropietario(usuarioId, prospecto);

        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        if (propiedad.getUsuario() == null || propiedad.getUsuario().getId() == null) {
            throw new IllegalArgumentException("La propiedad no tiene usuario asociado");
        }
        if (propiedad.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No puedes notificar una propiedad propia");
        }
        if (!prospecto.cumpleConPropiedad(propiedad)) {
            throw new IllegalArgumentException("La propiedad no es compatible con este prospecto");
        }

        Usuario remitente = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        String nombreRemitente = remitente.getNombreNegocio() != null && !remitente.getNombreNegocio().isBlank()
                ? remitente.getNombreNegocio()
                : remitente.getUsername();
        String logoUrl = remitente.getLogoInmobiliaria() != null
                ? remitente.getLogoInmobiliaria().getImageUrl()
                : null;

        pushSubscriptionRepository.findByUserId(propiedad.getUsuario().getId())
                .forEach(sub -> pushNotificationService.enviarNotificacionConIcono(
                        sub,
                        "🔔 Recomendación de prospecto",
                        String.format("%s te recomendó un prospecto compatible.", nombreRemitente),
                        logoUrl
                ));
    }


    private void validarPropietario(Long usuarioId, Prospecto prospecto) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (prospecto == null) {
            throw new IllegalArgumentException("El prospecto es obligatorio");
        }
        if (prospecto.getUsuario() == null || prospecto.getUsuario().getId() == null) {
            throw new IllegalArgumentException("El prospecto no tiene usuario asociado");
        }
        if (!prospecto.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permisos para modificar este prospecto");
        }
    }
}
