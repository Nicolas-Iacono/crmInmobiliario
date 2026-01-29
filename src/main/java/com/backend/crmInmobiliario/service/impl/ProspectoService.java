package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.prospecto.ProspectoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ProspectoModificacionDto;
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
                .addMapping(ProspectoEntradaDto::getCantidadAmbientes, Prospecto::setCantidadPersonas)
                .addMapping(ProspectoEntradaDto::getRangoPrecioMin, Prospecto::setRangoPrecioMin)
                .addMapping(ProspectoEntradaDto::getRangoPrecioMax, Prospecto::setRangoPrecioMax)
                .addMapping(ProspectoEntradaDto::getCochera, Prospecto::setCochera)
                .addMapping(ProspectoEntradaDto::getPatio, Prospecto::setPatio)
                .addMapping(ProspectoEntradaDto::getJardin, Prospecto::setJardin);

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
                .addMapping(Prospecto::getJardin, ProspectoSalidaDto::setJardin);

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

        // 4) Guardar
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
            dto.setUsuarioDtoSalida(modelMapper.map(prospecto.getUsuario(), UsuarioDtoSalida.class));
        }
        return dto;
    }

    public void notificarProspectosCompatiblesPorPropiedad(Propiedad propiedad) {
        if (propiedad == null || propiedad.getUsuario() == null || propiedad.getUsuario().getId() == null) return;

        Long ownerId = propiedad.getUsuario().getId();

        // Prospectos de otros
        List<Prospecto> prospectos = prospectoRepository.findByUsuarioIdNot(ownerId);

        long matches = prospectos.stream()
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
                .filter(p -> p.cumpleConPropiedad(propiedad))
                .map(this::mapProspectoSalida) // o modelMapper directo
                .toList();
    }
}
