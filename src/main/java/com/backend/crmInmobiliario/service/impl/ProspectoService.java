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
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
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

    @Override
    @Transactional
    public ProspectoSalidaDto crearProspecto(Long usuarioId, ProspectoEntradaDto dto) throws ResourceNotFoundException {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Prospecto prospecto = new Prospecto();
        prospecto.setUsuario(usuario);
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
        notificarPropiedadesCompatibles(guardado);
        return mapSalida(guardado);
    }

    @Override
    @Transactional
    public ProspectoSalidaDto actualizarProspecto(Long usuarioId, Long id, ProspectoModificacionDto dto) throws ResourceNotFoundException {
        Prospecto prospecto = prospectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));
        validarPropietario(usuarioId, prospecto);

        if (dto.getRangoPrecioMin() != null) prospecto.setRangoPrecioMin(dto.getRangoPrecioMin());
        if (dto.getRangoPrecioMax() != null) prospecto.setRangoPrecioMax(dto.getRangoPrecioMax());
        if (dto.getCantidadPersonas() != null) prospecto.setCantidadPersonas(dto.getCantidadPersonas());
        if (dto.getZonaPreferencia() != null) prospecto.setZonaPreferencia(dto.getZonaPreferencia());
        if (dto.getCantidadAmbientes() != null) prospecto.setCantidadAmbientes(dto.getCantidadAmbientes());
        if (dto.getCochera() != null) prospecto.setCochera(dto.getCochera());
        if (dto.getPatio() != null) prospecto.setPatio(dto.getPatio());
        if (dto.getJardin() != null) prospecto.setJardin(dto.getJardin());
        if (dto.getPileta() != null) prospecto.setPileta(dto.getPileta());

        Prospecto actualizado = prospectoRepository.save(prospecto);
        return mapSalida(actualizado);
    }

    @Override
    @Transactional
    public List<ProspectoSalidaDto> listarProspectosPorUsuario(Long usuarioId) throws ResourceNotFoundException {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }

        return prospectoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::mapSalida)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarProspecto(Long usuarioId, Long id) throws ResourceNotFoundException {
        Prospecto prospecto = prospectoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));
        validarPropietario(usuarioId, prospecto);
        prospectoRepository.delete(prospecto);
    }

    @Override
    @Transactional
    public List<PropiedadSalidaDto> listarPropiedadesCompatibles(Long usuarioId, Long prospectoId) throws ResourceNotFoundException {
        Prospecto prospecto = prospectoRepository.findById(prospectoId)
                .orElseThrow(() -> new ResourceNotFoundException("Prospecto no encontrado"));
        validarPropietario(usuarioId, prospecto);

        return propiedadRepository.findByUsuarioIdNot(usuarioId).stream()
                .filter(prospecto::cumpleConPropiedad)
                .map(propiedad -> modelMapper.map(propiedad, PropiedadSalidaDto.class))
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

    private ProspectoSalidaDto mapSalida(Prospecto prospecto) {
        ProspectoSalidaDto dto = modelMapper.map(prospecto, ProspectoSalidaDto.class);
        if (prospecto.getUsuario() != null) {
            dto.setUsuarioDtoSalida(modelMapper.map(prospecto.getUsuario(), UsuarioDtoSalida.class));
        }
        return dto;
    }

    private void notificarPropiedadesCompatibles(Prospecto prospecto) {
        List<Propiedad> propiedades = propiedadRepository.findByUsuarioIdNot(prospecto.getUsuario().getId());
        propiedades.stream()
                .filter(prospecto::cumpleConPropiedad)
                .forEach(propiedad -> {
                    pushSubscriptionRepository.findByUserId(propiedad.getUsuario().getId())
                            .forEach(sub -> pushNotificationService.enviarNotificacion(
                                    sub,
                                    "🏠 Nuevo prospecto disponible",
                                    String.format("Un prospecto busca una propiedad en %s.",
                                            prospecto.getZonaPreferencia() != null ? prospecto.getZonaPreferencia() : "tu zona")
                            ));
                });
        LOGGER.info("Notificaciones de prospecto enviadas para prospecto {}", prospecto.getId());
    }

    private void validarPropietario(Long usuarioId, Prospecto prospecto) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (prospecto.getUsuario() == null || !prospecto.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permisos para modificar este prospecto");
        }
    }
}
