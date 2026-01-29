package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.propiedades.PropiedadEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.PropiedadModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.*;
import com.backend.crmInmobiliario.DTO.salida.prospecto.ProspectoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Propietario;
import com.backend.crmInmobiliario.entity.Prospecto;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.ProspectoRepository;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IPropiedadService;
import com.backend.crmInmobiliario.service.impl.notificacionesPush.PushNotificationService;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class PropiedadService implements IPropiedadService {
    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private InquilinoRepository inquilinoRepository;

    @Autowired
    private PropiedadRepository propiedadRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProspectoRepository prospectoRepository;
    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;
    @Autowired
    private PushNotificationService pushNotificationService;

    public PropiedadService(ModelMapper modelMapper, InquilinoRepository inquilinoRepository, PropiedadRepository propiedadRepository, PropietarioRepository propietarioRepository) {
        this.modelMapper = modelMapper;
        this.inquilinoRepository = inquilinoRepository;
        this.propiedadRepository = propiedadRepository;
        this.propietarioRepository = propietarioRepository;
        configureMapping();
    }

    private void configureMapping() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true); // Ignorar ambigüedad en el mapeo.
        modelMapper.typeMap(PropiedadEntradaDto.class, Propiedad.class)
                .addMapping(PropiedadEntradaDto::getId_propietario, Propiedad::setPropietario)
                .addMapping(PropiedadEntradaDto::getNombreUsuario, Propiedad::setUsuario);

        modelMapper.typeMap(Propiedad.class, PropiedadSalidaDto.class)
                .addMapping(Propiedad::getPropietario, PropiedadSalidaDto::setPropietarioSalidaDto)
                .addMapping(Propiedad::getUsuario, PropiedadSalidaDto::setUsuarioDtoSalida);

    }

    @Transactional
    @Override
    public List<PropiedadSoloSalidaDto> listarPropiedades() {
        List<Propiedad> propiedades = propiedadRepository.findAll();

        return propiedades.stream()
                .filter(Objects::nonNull)
                .map(propiedad -> {
                    PropiedadSoloSalidaDto dto = modelMapper.map(propiedad, PropiedadSoloSalidaDto.class);

                    if (propiedad.getImagenes() != null && !propiedad.getImagenes().isEmpty()) {
                        List<ImgUrlSalidaDto> imagenesDto = propiedad.getImagenes().stream()
                                .map(img -> {
                                    ImgUrlSalidaDto imgDto = new ImgUrlSalidaDto();
                                    imgDto.setIdImage(img.getIdImage());
                                    imgDto.setImageUrl(img.getImageUrl());
                                    imgDto.setNombreOriginal(img.getNombreOriginal());
                                    imgDto.setTipoImagen(img.getTipoImagen());
                                    imgDto.setFechaSubida(img.getFechaSubida());
                                    return imgDto;
                                })
                                .toList();
                        dto.setImagenes(imagenesDto);
                    } else {
                        dto.setImagenes(Collections.emptyList());
                    }

                    return dto;
                })
                .toList();
    }


    @Override
    @Transactional
    public PropiedadSalidaDto crearPropiedad(PropiedadEntradaDto propiedadEntradaDto, Long propietarioId)
            throws ResourceNotFoundException {

        String nombreUsuario = propiedadEntradaDto.getNombreUsuario();

        Usuario usuario = usuarioRepository.findUserByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Propiedad propiedad = new Propiedad();
        propiedad.setDireccion(propiedadEntradaDto.getDireccion());
        propiedad.setLocalidad(propiedadEntradaDto.getLocalidad());
        propiedad.setPartido(propiedadEntradaDto.getPartido());
        propiedad.setProvincia(propiedadEntradaDto.getProvincia());
        propiedad.setTipo(propiedadEntradaDto.getTipo());
        propiedad.setInventario(propiedadEntradaDto.getInventario());
        propiedad.setDisponibilidad(propiedadEntradaDto.getDisponibilidad());
        propiedad.setCantidadAmbientes(propiedadEntradaDto.getCantidadAmbientes());
        propiedad.setPileta(propiedadEntradaDto.getPileta());
        propiedad.setCochera(propiedadEntradaDto.getCochera());
        propiedad.setJardin(propiedadEntradaDto.getJardin());
        propiedad.setPatio(propiedadEntradaDto.getPatio());
        propiedad.setUsuario(usuario);

        // asignar propietario solo si viene
        if (propiedadEntradaDto.getId_propietario() != null) {
            Propietario propietario = propietarioRepository.findById(propiedadEntradaDto.getId_propietario())
                    .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

            if (!propietario.getUsuario().getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("El propietario no pertenece al mismo usuario");
            }

            propiedad.setPropietario(propietario);
        }

        Propiedad propiedadAPersistir = propiedadRepository.save(propiedad);

        boolean propiedadActiva = cambiarDisponibilidadPropiedad(propiedadAPersistir.getId_propiedad());
        if (!propiedadActiva) {
            throw new RuntimeException("No se pudo activar la propiedad");
        }

        // mapear la entidad al DTO de salida
        PropiedadSalidaDto propiedadSalidaDto = modelMapper.map(propiedadAPersistir, PropiedadSalidaDto.class);

        if (propiedadAPersistir.getPropietario() != null) {
            PropietarioContratoDtoSalida propietarioSalidaDto =
                    modelMapper.map(propiedadAPersistir.getPropietario(), PropietarioContratoDtoSalida.class);
            propiedadSalidaDto.setPropietarioSalidaDto(propietarioSalidaDto);
        } else {
            propiedadSalidaDto.setPropietarioSalidaDto(null);
        }

        notificarProspectosPorPropiedad(propiedadAPersistir);
        return propiedadSalidaDto;
    }

    @Transactional
    @Override
    public Boolean cambiarDisponibilidadPropiedad(Long id) throws ResourceNotFoundException {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Propiedad no encontrado"));
        propiedad.setDisponibilidad(!propiedad.isDisponibilidad());
        propiedadRepository.save(propiedad);
        return propiedad.isDisponibilidad();
    }

    @Override
    @Transactional
    public List<PropiedadSalidaDto> buscarPropiedadesPorUsuario(String username) {
        List<Propiedad> propiedadList = propiedadRepository.findPropiedadByUsername(username);
        return propiedadList.stream()
                .map(propiedad -> modelMapper.map(propiedad, PropiedadSalidaDto.class))
                .toList();
    }
    @Transactional
    public PropiedadSalidaDto asignarPropietario(Long propiedadId, Long propietarioId) throws ResourceNotFoundException {
        Propiedad p = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        Propietario prop = propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

        if (!prop.getUsuario().getId().equals(p.getUsuario().getId())) {
            throw new IllegalArgumentException("El propietario no pertenece al mismo usuario");
        }

        p.setPropietario(prop);
        Propiedad saved = propiedadRepository.save(p);

        return modelMapper.map(saved, PropiedadSalidaDto.class);
    }
    @Transactional
    public PropiedadSalidaDto quitarPropietario(Long propiedadId) throws ResourceNotFoundException {
        Propiedad p = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        p.setPropietario(null);
        Propiedad saved = propiedadRepository.save(p);

        PropiedadSalidaDto out = modelMapper.map(saved, PropiedadSalidaDto.class);
        out.setPropietarioSalidaDto(null);
        return out;
    }
    @Override
    @Transactional
    public PropiedadSalidaDto buscarPropiedadPorId(Long id)throws ResourceNotFoundException {
       Propiedad propiedad = propiedadRepository.findById(id).orElse(null);
       PropiedadSalidaDto propiedadSalidaDto =  null;
       if(propiedad != null){
           propiedadSalidaDto = modelMapper.map(propiedad, PropiedadSalidaDto.class);

           List<ImgUrlSalidaDto> imagenesDto = propiedad.getImagenes()
                   .stream()
                   .map(img -> modelMapper.map(img, ImgUrlSalidaDto.class))
                   .toList();

           propiedadSalidaDto.setImagenes(imagenesDto);


       }else{
           throw new ResourceNotFoundException("No se encontro la propiedad buscada");
       }
        return propiedadSalidaDto;
    }

    @Override
    @Transactional
    public PropiedadSalidaDto actualizarPropiedad(Long id, PropiedadModificacionDto dto) throws ResourceNotFoundException {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        if (dto.getDireccion() != null) propiedad.setDireccion(dto.getDireccion());
        if (dto.getLocalidad() != null) propiedad.setLocalidad(dto.getLocalidad());
        if (dto.getPartido() != null) propiedad.setPartido(dto.getPartido());
        if (dto.getProvincia() != null) propiedad.setProvincia(dto.getProvincia());
        if (dto.getTipo() != null) propiedad.setTipo(dto.getTipo());
        if (dto.getInventario() != null) propiedad.setInventario(dto.getInventario());
        if (dto.getDisponibilidad() != null) propiedad.setDisponibilidad(dto.getDisponibilidad());
        if (dto.getCantidadAmbientes() != null) propiedad.setCantidadAmbientes(dto.getCantidadAmbientes());
        if (dto.getPileta() != null) propiedad.setPileta(dto.getPileta());
        if (dto.getCochera() != null) propiedad.setCochera(dto.getCochera());
        if (dto.getJardin() != null) propiedad.setJardin(dto.getJardin());
        if (dto.getPatio() != null) propiedad.setPatio(dto.getPatio());

        if (dto.getPropietarioId() != null) {
            Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));
            if (!propietario.getUsuario().getId().equals(propiedad.getUsuario().getId())) {
                throw new IllegalArgumentException("El propietario no pertenece al mismo usuario");
            }
            propiedad.setPropietario(propietario);
        }

        Propiedad saved = propiedadRepository.save(propiedad);
        return modelMapper.map(saved, PropiedadSalidaDto.class);
    }

    @Override
    @Transactional
    public List<ProspectoSalidaDto> listarProspectosCompatibles(Long propiedadId, Long usuarioId) throws ResourceNotFoundException {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        if (usuarioId == null || propiedad.getUsuario() == null || !propiedad.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Propiedad no encontrada");
        }

        return prospectoRepository.findByUsuarioIdNot(usuarioId).stream()
                .filter(prospecto -> prospecto.cumpleConPropiedad(propiedad))
                .map(this::mapProspectoSalidaPublica)
                .toList();
    }

    @Override
    @Transactional
    public void eliminarPropiedad(Long id) throws ResourceNotFoundException {
        // 1) Traer con imágenes cargadas
        Propiedad p = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la propiedad con el id proporcionado."));

        // 2) Romper relación con el propietario para que no la re-persistan al flush
        Propietario prop = p.getPropietario();
        if (prop != null && prop.getPropiedades() != null) {
            prop.getPropiedades().remove(p); // <- clave
        }
        p.setPropietario(null);

        // 3) Cargar y cortar hijos (lado dueño) antes de clear
        p.getImagenes().forEach(img -> img.setPropiedad(null));
        p.getImagenes().clear();

        // 4) Borrar y forzar flush para ver los DELETE ya
        propiedadRepository.delete(p);
        propiedadRepository.flush();

        System.out.println("Propiedad eliminada: " + id);
    }

    @Override
    @Transactional
    public Integer enumerarPropiedades(String username) {
      return propiedadRepository.countByUsuarioUsername(username);

    }

    private void notificarProspectosPorPropiedad(Propiedad propiedad) {
        List<Prospecto> prospectos = prospectoRepository.findByUsuarioIdNot(propiedad.getUsuario().getId()).stream()
                .filter(prospecto -> prospecto.cumpleConPropiedad(propiedad))
                .toList();
        if (prospectos.isEmpty()) {
            return;
        }

        int total = prospectos.size();
        pushSubscriptionRepository.findByUserId(propiedad.getUsuario().getId())
                .forEach(sub -> pushNotificationService.enviarNotificacion(
                        sub,
                        "📌 Prospectos encontrados para tu propiedad",
                        String.format("Hay %d prospecto(s) que buscan una propiedad en %s.",
                                total,
                                propiedad.getLocalidad() != null ? propiedad.getLocalidad() : "tu zona")
                ));
    }

    private ProspectoSalidaDto mapProspectoSalidaPublica(Prospecto prospecto) {
        ProspectoSalidaDto dto = modelMapper.map(prospecto, ProspectoSalidaDto.class);
        dto.setUsuarioDtoSalida(null);
        return dto;
    }

}
