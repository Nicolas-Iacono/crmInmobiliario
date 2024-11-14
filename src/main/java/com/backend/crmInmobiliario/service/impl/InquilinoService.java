package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.InquilinoEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.PropiedadRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IInquilinoService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InquilinoService implements IInquilinoService {
    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);
    private ModelMapper modelMapper;
    private InquilinoRepository inquilinoRepository;
    private PropiedadRepository propiedadRepository;
    private UsuarioRepository usuarioRepository;


    public InquilinoService(   UsuarioRepository usuarioRepository,ModelMapper modelMapper, InquilinoRepository inquilinoRepository, PropiedadRepository propiedadRepository) {
        this.modelMapper = modelMapper;
        this.inquilinoRepository = inquilinoRepository;
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        configureMapping();

    }

    private void configureMapping() {
    }

    @Override
    public List<InquilinoSalidaDto> listarInquilinos() {
        List<Inquilino> inquilinos = inquilinoRepository.findAll();
        return inquilinos.stream()
                .map(inquilino -> modelMapper.map(inquilino, InquilinoSalidaDto.class))
        .toList();
    }

    @Override
    public InquilinoSalidaDto crearInquilino(InquilinoEntradaDto inquilinoEntradaDto) throws ResourceNotFoundException {

        String nombreUsuario = inquilinoEntradaDto.getNombreUsuario();
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo o vacío");
        }
        LOGGER.info("Intentando encontrar usuario con username: " + nombreUsuario);
        Usuario usuario = usuarioRepository.findUserByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Inquilino inquilino = new Inquilino();
        inquilino.setPronombre(inquilinoEntradaDto.getPronombre());
        inquilino.setNombre(inquilinoEntradaDto.getNombre());
        inquilino.setApellido(inquilinoEntradaDto.getApellido());
        inquilino.setEmail(inquilinoEntradaDto.getEmail());
        inquilino.setDni(inquilinoEntradaDto.getDni());
        inquilino.setTelefono(inquilinoEntradaDto.getTelefono());
        inquilino.setDireccionResidencial(inquilinoEntradaDto.getDireccionResidencial());
        inquilino.setCuit(inquilinoEntradaDto.getCuit());
        inquilino.setEstadoCivil(inquilinoEntradaDto.getEstadoCivil());
        inquilino.setNacionalidad(inquilinoEntradaDto.getNacionalidad());
        inquilino.setUsuario(usuario);

        Inquilino inquilinoToSave = inquilinoRepository.save(inquilino);
        InquilinoSalidaDto inquilinoSalidaDto = modelMapper.map(inquilinoToSave, InquilinoSalidaDto.class);
        return inquilinoSalidaDto;
    }

    @Override
    public InquilinoSalidaDto buscarInquilinoPorId(Long id) throws ResourceNotFoundException {
        Inquilino inquilino = inquilinoRepository.findById(id).orElse(null);
        InquilinoSalidaDto inquilinoSalidaDto = null;
        if(inquilino != null){
            inquilinoSalidaDto = modelMapper.map(inquilino, InquilinoSalidaDto.class);
        }else{
            throw new ResourceNotFoundException("No se encontró el inquilino con el ID proporcionado");
        }
        return inquilinoSalidaDto;
    }

    @Override
    @Transactional
    public void eliminarInquilino(Long id) throws ResourceNotFoundException {
        Inquilino inquilino = inquilinoRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro el inquilino con el id proporcionado!!"));
        inquilinoRepository.delete(inquilino);
    }

    @Override
    public List<InquilinoSalidaDto> buscarInquilinoPorUsuario(String username) {
        List<Inquilino> inquilinoList = inquilinoRepository.findInquilinoByUsername(username);
        return inquilinoList.stream()
                .map(inquilino -> modelMapper.map(inquilino, InquilinoSalidaDto.class))
                .toList();
    }
}
