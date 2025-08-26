package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.propiedades.PropiedadEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.*;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Propietario;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IPropiedadService;
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
    public PropiedadSalidaDto crearPropiedad(PropiedadEntradaDto propiedadEntradaDto, Long propietarioId) throws ResourceNotFoundException {

        String nombreUsuario = propiedadEntradaDto.getNombreUsuario();

        Usuario usuario = usuarioRepository.findUserByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Buscar el propietario con propietarioId
        Propietario propietario = propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

        // Crear la propiedad con los datos del DTO
        Propiedad propiedad = new Propiedad();
        propiedad.setDireccion(propiedadEntradaDto.getDireccion());
        propiedad.setLocalidad(propiedadEntradaDto.getLocalidad());
        propiedad.setPartido(propiedadEntradaDto.getPartido());
        propiedad.setProvincia(propiedadEntradaDto.getProvincia());
        propiedad.setTipo(propiedadEntradaDto.getTipo());
        propiedad.setInventario(propiedadEntradaDto.getInventario());
        propiedad.setDisponibilidad(propiedadEntradaDto.getDisponibilidad());
        // Asignar el propietario ya encontrado
        propiedad.setPropietario(propietario);
        propiedad.setUsuario(usuario);

        // Guardar la propiedad en la base de datos
        Propiedad propiedadAPersistir = propiedadRepository.save(propiedad);

        boolean propiedadActiva = cambiarDisponibilidadPropiedad(propiedadAPersistir.getId_propiedad());
        if (!propiedadActiva) {
            throw new RuntimeException("No se pudo activar el contrato");
        }
        // Mapear la entidad guardada al DTO de salida
        PropiedadSalidaDto propiedadSalidaDto = modelMapper.map(propiedadAPersistir, PropiedadSalidaDto.class);
        PropietarioContratoDtoSalida propietarioSalidaDto = modelMapper.map(propiedadAPersistir.getPropietario(), PropietarioContratoDtoSalida.class);
        propiedadSalidaDto.setPropietarioSalidaDto(propietarioSalidaDto);

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

}
