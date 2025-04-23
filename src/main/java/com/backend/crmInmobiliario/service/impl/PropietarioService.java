package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.PropietarioEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Propiedad;
import com.backend.crmInmobiliario.entity.Propietario;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IPropietarioService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PropietarioService implements IPropietarioService {
    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);
    private ModelMapper modelMapper;
    private InquilinoRepository inquilinoRepository;
    private PropiedadRepository propiedadRepository;
    private PropietarioRepository propietarioRepository;
    private UsuarioRepository usuarioRepository;

    public PropietarioService(ModelMapper modelMapper, InquilinoRepository inquilinoRepository, PropiedadRepository propiedadRepository, PropietarioRepository propietarioRepository, UsuarioRepository usuarioRepository) {
        this.modelMapper = modelMapper;
        this.inquilinoRepository = inquilinoRepository;
        this.propiedadRepository = propiedadRepository;
        this.propietarioRepository = propietarioRepository;
        this.usuarioRepository = usuarioRepository;
        configureMapping();
    }

    private void configureMapping() {
    }

    @Transactional
    @Override
    public List<PropietarioSalidaDto> listarPropietarios() {
       List<Propietario> propietarios = propietarioRepository.findAll();
        return propietarios.stream()
                .map(garante -> {
                    PropietarioSalidaDto dto = modelMapper.map(propietarios, PropietarioSalidaDto.class);

//                    List<ImgUrlSalidaDto> imagenesDto = dto.getImagenes()
//                            .stream()
//                            .map(img -> modelMapper.map(img, ImgUrlSalidaDto.class))
//                            .toList();
//
//                    dto.setImagenes(imagenesDto);
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public PropietarioSalidaDto crearPropietario(PropietarioEntradaDto propietarioEntradaDto) throws ResourceNotFoundException {
        String nombreUsuario = propietarioEntradaDto.getNombreUsuario();
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo o vacío");
        }
        LOGGER.info("Intentando encontrar usuario con username: " + nombreUsuario);
        Usuario usuario = usuarioRepository.findUserByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));



        Propietario propietario = new Propietario();
        propietario.setPronombre(propietarioEntradaDto.getPronombre());
        propietario.setNombre(propietarioEntradaDto.getNombre());
        propietario.setApellido(propietarioEntradaDto.getApellido());
        propietario.setEmail(propietarioEntradaDto.getEmail());
        propietario.setTelefono(propietarioEntradaDto.getTelefono());
        propietario.setDni(propietarioEntradaDto.getDni());
        propietario.setCuit(propietarioEntradaDto.getCuit());
        propietario.setDireccionResidencial(propietarioEntradaDto.getDireccionResidencial());
        propietario.setNacionalidad(propietarioEntradaDto.getNacionalidad());
        propietario.setEstadoCivil(propietarioEntradaDto.getEstadoCivil());
        propietario.setUsuario(usuario);

        List<Propiedad> propiedades = propietarioEntradaDto.getPropiedades() != null
                ? propietarioEntradaDto.getPropiedades().stream()
                .map(propiedadDto -> {
                    Propiedad propiedad = new Propiedad();
                    propiedad.setDireccion(propiedadDto.getDireccion());
                    propiedad.setPartido(propiedadDto.getPartido());
                    propiedad.setLocalidad(propiedadDto.getLocalidad());
                    propiedad.setProvincia(propiedadDto.getProvincia());
                    propiedad.setDisponibilidad(propiedadDto.getDisponibilidad());
                    return propiedad;
                }).toList()
                : new ArrayList<>();  // Si es null, se asigna una lista vacía n

    propietario.setPropiedades(propiedades);
        // Guardar el propietario en la base de datos
        Propietario propietarioGuardado = propietarioRepository.save(propietario);

        // Convertir el propietario guardado a un DTO de salida usando ModelMapper (o manualmente)
        PropietarioSalidaDto propietarioSalidaDto = modelMapper.map(propietarioGuardado, PropietarioSalidaDto.class);

        // Devolver el DTO de salida
        return propietarioSalidaDto;
    }

    @Transactional
    @Override
    public PropietarioSalidaDto buscarPropietarioPorId(Long id) throws ResourceNotFoundException {
        Propietario propietario = propietarioRepository.findById(id).orElse(null);
        PropietarioSalidaDto propietarioSalidaDto = null;
        if(propietario != null){
            propietarioSalidaDto = modelMapper.map(propietario, PropietarioSalidaDto.class);

//            List<ImgUrlSalidaDto> imagenesDto = propietario.getImagenes()
//                    .stream()
//                    .map(img -> modelMapper.map(img, ImgUrlSalidaDto.class))
//                    .toList();
//
//            propietarioSalidaDto.setImagenes(imagenesDto);

        }else{
            throw new ResourceNotFoundException("No se encontró el propietario con el ID proporcionado");
        }
        return propietarioSalidaDto;
    }

    @Override
    public void eliminarPropietario(Long id) throws ResourceNotFoundException {
        Propietario propietario = propietarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado."));
        propietarioRepository.delete(propietario);
    }

    @Override
    @Transactional
    public List<PropietarioSalidaDto> buscarPropietariosPorUsuario(String username) {
        List<Propietario> propietarioList = propietarioRepository.findPropietarioByUsername(username);
        return propietarioList.stream()
                .map(propietario -> modelMapper.map(propietario, PropietarioSalidaDto.class))
                .toList();
    }
    }

