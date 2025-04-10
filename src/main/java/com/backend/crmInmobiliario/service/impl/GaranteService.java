package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.garante.GaranteEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.Garante;
import com.backend.crmInmobiliario.entity.Inquilino;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.GaranteRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IGaranteService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GaranteService implements IGaranteService {

    private static final String UPLOAD_DIR = "https://srv1597-files.hstgr.io/cb06a4ee9e063e7f/files/public_html/uploads";
    private final Logger LOGGER = LoggerFactory.getLogger(GaranteService.class);
    private ModelMapper modelMapper;

    private ContratoRepository contratoRepository;
    private GaranteRepository garanteRepository;
    private UsuarioRepository usuarioRepository;

    public GaranteService(ModelMapper modelMapper, ContratoRepository contratoRepository, GaranteRepository garanteRepository,UsuarioRepository usuarioRepository) {
        this.modelMapper = modelMapper;
        this.contratoRepository = contratoRepository;
        this.garanteRepository = garanteRepository;
        this.usuarioRepository = usuarioRepository;
        configureMapping();
    }

    private void configureMapping() {
        modelMapper.typeMap(GaranteEntradaDto.class, Garante.class);
        modelMapper.typeMap(Garante.class, GaranteSalidaDto.class)
                .addMapping(Garante::getUsuario, GaranteSalidaDto::setUsuarioDtoSalida);


    }
    public void deleteByContratoId(Long contratoId) {
        garanteRepository.deleteByContratoId(contratoId);
    }

    @Override
    public List<GaranteSalidaDto> buscarGarantePorUsuario(String username) {
        List<Garante> garanteList = garanteRepository.findGaranteByUsername(username);
        return garanteList.stream()
                .map(garante -> modelMapper.map(garante, GaranteSalidaDto.class))
                .toList();
    }

    @Override
    public List<GaranteSalidaDto> listarGarantes() {
        List<Garante> garantes = garanteRepository.findAll();
        return garantes.stream()
                .map(garante -> modelMapper.map(garante,GaranteSalidaDto.class))
                .toList();
    }

    @Override
    @Transactional
    public GaranteSalidaDto crearGarante(GaranteEntradaDto garanteEntradaDto) throws ResourceNotFoundException{
        String nombreUsuario = garanteEntradaDto.getNombreUsuario();
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo o vacío");
        }
        LOGGER.info("Intentando encontrar usuario con username: " + nombreUsuario);
        Usuario usuario = usuarioRepository.findUserByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        LOGGER.info("usuario encontrado con username: " + nombreUsuario);

        Garante garante = new Garante();
        garante.setPronombre(garanteEntradaDto.getPronombre());
        garante.setNombre(garanteEntradaDto.getNombre());
        garante.setApellido(garanteEntradaDto.getApellido());
        garante.setEmail(garanteEntradaDto.getEmail());
        garante.setDni(garanteEntradaDto.getDni());
        garante.setTelefono(garanteEntradaDto.getTelefono());
        garante.setDireccionResidencial(garanteEntradaDto.getDireccionResidencial());
        garante.setCuit(garanteEntradaDto.getCuit());
        garante.setCargoActual(garanteEntradaDto.getCargoActual());
        garante.setCuitEmpresa(garanteEntradaDto.getCuitEmpresa());
        garante.setPronombre(garanteEntradaDto.getPronombre());
        garante.setLegajo((long) garanteEntradaDto.getLegajo());
        garante.setSectorActual(garanteEntradaDto.getSectorActual());
        garante.setNombreEmpresa(garanteEntradaDto.getNombreEmpresa());
        garante.setEstadoCivil(garanteEntradaDto.getEstadoCivil());
        garante.setNacionalidad(garanteEntradaDto.getNacionalidad());
        garante.setTipoGarantia(garanteEntradaDto.getTipoGarantia());
        garante.setTipoPropiedad(garanteEntradaDto.getTipoPropiedad());
        garante.setPartidaInmobiliaria(garanteEntradaDto.getPartidaInmobiliaria());
        garante.setEstadoOcupacion(garanteEntradaDto.getEstadoOcupacion());
        garante.setDireccion(garanteEntradaDto.getDireccion());
        garante.setInfoCatastral(garanteEntradaDto.getInfoCatastral());
        garante.setInformeDominio(garanteEntradaDto.getInformeDominio());
        garante.setInformeInhibicion(garanteEntradaDto.getInformeInhibicion());
        garante.setUsuario(usuario);

        Garante garanteToSave = garanteRepository.save(garante);
        LOGGER.info("garanteToSave con username: " + garanteToSave.getUsuario().getUsername());

        GaranteSalidaDto garanteSalidaDto = modelMapper.map(garanteToSave, GaranteSalidaDto.class);

        LOGGER.info("garanteSalidaDto usuario con username: " + garanteSalidaDto.getUsuarioDtoSalida().getUsername());

        return garanteSalidaDto;
    }


    @Override
    public GaranteSalidaDto listarGarantePorId(Long id) throws ResourceNotFoundException {
        Garante garante = garanteRepository.findById(id).orElse(null);
        GaranteSalidaDto garanteSalidaDto = null;
        if(garante !=null){
            garanteSalidaDto = modelMapper.map(garante, GaranteSalidaDto.class);
        }else{
            throw new ResourceNotFoundException("No se encontró el garante con el ID proporcionado");
        }
        return garanteSalidaDto;
    }

    @Override
    public void eliminarGarante(Long id) throws ResourceNotFoundException {
        Garante garante = garanteRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("No se encontro el garante con el id: " + id));
        garanteRepository.delete(garante);
    }

    @Override
    public void asignarGarante(Long idGarante, Long idContrato) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra el contrato con el id proporcionado!!"));

        Garante garante = garanteRepository.findById(idGarante)
                .orElseThrow(() -> new ResourceNotFoundException("No se encuentra el garante con el id proporcionado!!"));

        // Relación bidireccional
        garante.setContrato(contrato);

        List<Garante> garantes = contrato.getGarantes();
        if (garantes == null) {
            garantes = new ArrayList<>();  // Inicializa la lista si es null
        }
        garantes.add(garante); // HashSet evita duplicados automáticamente

        contrato.setGarantes(garantes);

        contratoRepository.save(contrato);
        garanteRepository.save(garante);
    }
}
