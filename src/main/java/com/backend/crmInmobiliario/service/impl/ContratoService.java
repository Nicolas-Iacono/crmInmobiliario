package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ContratoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.LatestContratosSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.entity.impuestos.Agua;
import com.backend.crmInmobiliario.entity.impuestos.Gas;
import com.backend.crmInmobiliario.entity.impuestos.Luz;
import com.backend.crmInmobiliario.entity.impuestos.Municipal;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.*;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.IContratoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.hibernate.collection.spi.PersistentBag;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContratoService implements IContratoService {

    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);

    private ContratoRepository contratoRepository;
    private ModelMapper modelMapper;

    private InquilinoRepository inquilinoRepository;

    private PropietarioRepository propietarioRepository;

    private PropiedadRepository propiedadRepository;

    private GaranteRepository garanteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private GasRepository gasRepository;
    private AguaRepository aguaRepository;
    private LuzRepository luzRepository;
    private MunicipalRepository municipalRepository;
//    private PdfContratoRepository pdfContratoRepository;

    public ContratoService(ContratoRepository contratoRepository,
                           GaranteRepository garanteRepository,
                           ModelMapper modelMapper,
                           InquilinoRepository inquilinoRepository,
                           PropietarioRepository propietarioRepository,
                           PropiedadRepository propiedadRepository,
                           GasRepository gasRepository,
                           AguaRepository aguaRepository,
                           LuzRepository luzRepository,
                           MunicipalRepository municipalRepository
//                           UsuarioRepository usuarioRepository,
                         ) {

        this.contratoRepository = contratoRepository;
        this.modelMapper = modelMapper;
        this.inquilinoRepository = inquilinoRepository;
        this.propietarioRepository = propietarioRepository;
        this.propiedadRepository = propiedadRepository;
        this.garanteRepository = garanteRepository;
        this.aguaRepository = aguaRepository;
        this.luzRepository = luzRepository;
        this.gasRepository = gasRepository;
        this.municipalRepository = municipalRepository;
        this.usuarioRepository = usuarioRepository;
//        this.pdfContratoRepository = pdfContratoRepository;
        configureMapping();
    }

    private void configureMapping() {

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true);

        modelMapper.createTypeMap(PersistentBag.class, List.class).setConverter(context -> {
            PersistentBag source = (PersistentBag) context.getSource();
            return source == null ? null : new ArrayList<>(source);
        });

        modelMapper.typeMap(ContratoEntradaDto.class, Contrato.class)
                .addMapping(ContratoEntradaDto::getId_inquilino, Contrato::setInquilino)
                .addMapping(ContratoEntradaDto::getId_propiedad, Contrato::setPropiedad)
                .addMapping(ContratoEntradaDto::getId_propietario, Contrato::setPropietario)
                .addMapping(ContratoEntradaDto::getGarantesIds, Contrato::setGarantes)
                .addMapping(ContratoEntradaDto::getId_agua, Contrato::setAgua)
                .addMapping(ContratoEntradaDto::getId_luz, Contrato::setLuz)
                .addMapping(ContratoEntradaDto::getId_gas, Contrato::setGas)
                .addMapping(ContratoEntradaDto::getId_municipal, Contrato::setMunicipal);

        modelMapper.typeMap(Contrato.class, ContratoSalidaDto.class)
                .addMapping(Contrato::getInquilino, ContratoSalidaDto::setInquilino)
                .addMapping(Contrato::getPropiedad, ContratoSalidaDto::setPropiedad)
                .addMapping(Contrato::getPropietario, ContratoSalidaDto::setPropietario)
                .addMapping(Contrato::getGarantes, ContratoSalidaDto::setGarantes)
                .addMapping(Contrato::getAgua, ContratoSalidaDto::setImpuestos)
                .addMapping(Contrato::getLuz, ContratoSalidaDto::setImpuestos)
                .addMapping(Contrato::getGas, ContratoSalidaDto::setImpuestos)
                .addMapping(Contrato::getMunicipal, ContratoSalidaDto::setImpuestos)
                .addMapping(Contrato::getTiempoRestante, ContratoSalidaDto::setTiempoRestante);

        modelMapper.typeMap(ContratoModificacionDto.class, ContratoSalidaDto.class)
                .addMapping(ContratoModificacionDto::getPdfContratoTexto, ContratoSalidaDto::setContratoPdf);
    }



    @Override
    public List<ContratoSalidaDto> listarContratos() {
        List<Contrato> contratos = contratoRepository.findAll();
        return contratos.stream()
                .map(contrato -> {
                    Long tiempoRestante = null;
                    try {
                        tiempoRestante = verificarFinalizacionContrato(contrato.getId_contrato());
                    } catch (ResourceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    contrato.setTiempoRestante(tiempoRestante);
                    return modelMapper.map(contrato, ContratoSalidaDto.class);
                })
                .toList();

    }

    @Transactional
    @Override
    public ContratoSalidaDto crearContrato(ContratoEntradaDto contratoEntradaDto) throws ResourceNotFoundException {

        validarContratoEntrada(contratoEntradaDto); // Separar validaciones en un método

        String nombreUsuario = contratoEntradaDto.getNombreUsuario();

        Usuario usuario = usuarioRepository.findUserByUsername(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));


        Propietario propietario = propietarioRepository.findById(contratoEntradaDto.getId_propietario())
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

        Inquilino inquilino = inquilinoRepository.findById(contratoEntradaDto.getId_inquilino())
                .orElseThrow(() -> new ResourceNotFoundException("Inquilino no encontrado"));

        Propiedad propiedad = propiedadRepository.findById(contratoEntradaDto.getId_propiedad())
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        Gas gas = gasRepository.findById(contratoEntradaDto.getId_gas())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio de gas no encontrado"));

        Luz luz = luzRepository.findById(contratoEntradaDto.getId_luz())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio de luz no encontrado"));

        Agua agua = aguaRepository.findById(contratoEntradaDto.getId_agua())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio de agua no encontrado"));

        Municipal municipal = municipalRepository.findById(contratoEntradaDto.getId_municipal())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio municipal no encontrado"));

        List<Garante> garantes = obtenerGarantesPorIds(contratoEntradaDto.getGarantesIds());

        validarInquilinoYPropiedadDisponibles(inquilino, propiedad);

        // Crear el contrato y mapear sus datos
        Contrato contratoEnCreacion = modelMapper.map(contratoEntradaDto, Contrato.class);
        asignarEntidadesRelacionadas(contratoEnCreacion, usuario, propietario, inquilino, propiedad, luz, gas, agua, municipal, garantes);

        // Persistir el contrato
        Contrato contratoPersistido = contratoRepository.save(contratoEnCreacion);

        // Cambiar el estado del contrato a activo
        if (!cambiarEstadoContrato(contratoPersistido.getId_contrato())) {
            throw new RuntimeException("No se pudo activar el contrato");
        }

        // Actualizar la disponibilidad de la propiedad
        propiedad.setDisponibilidad(false);
        propiedadRepository.save(propiedad);

        return modelMapper.map(contratoPersistido, ContratoSalidaDto.class);
    }

    private void validarContratoEntrada(ContratoEntradaDto dto) {
        if (dto.getNombreUsuario() == null || dto.getNombreUsuario().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo o vacío");
        }
        if (dto.getId_propietario() == null || dto.getId_propietario() <= 0) {
            throw new IllegalArgumentException("El ID del propietario no es válido.");
        }
        if (dto.getId_inquilino() == null || dto.getId_inquilino() <= 0) {
            throw new IllegalArgumentException("El ID del inquilino no es válido.");
        }
        // Validaciones adicionales aquí...
    }

    private List<Garante> obtenerGarantesPorIds(List<Long> garantesIds) throws ResourceNotFoundException {
        if (garantesIds == null || garantesIds.isEmpty()) {
            throw new IllegalArgumentException("La lista de garantes no puede estar vacía");
        }
        List<Garante> garantes = new ArrayList<>();
        for (Long id : garantesIds) {
            Garante garante = garanteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró el garante con el id " + id));
            garantes.add(garante);
        }
        return garantes;
    }

    private void validarInquilinoYPropiedadDisponibles(Inquilino inquilino, Propiedad propiedad) {
        Optional<Contrato> contratoExistente = contratoRepository.findByInquilinoAndActivoTrue(inquilino);
        if (contratoExistente.isPresent()) {
            throw new RuntimeException("El inquilino ya tiene un contrato activo y no puede ser asignado a otro contrato");
        }
        if (!propiedad.isDisponibilidad()) {
            throw new RuntimeException("La propiedad está asignada a otro contrato");
        }
    }

    private void asignarEntidadesRelacionadas(Contrato contrato, Usuario usuario, Propietario propietario, Inquilino inquilino,
                                              Propiedad propiedad, Luz luz, Gas gas, Agua agua, Municipal municipal, List<Garante> garantes) {
        contrato.setUsuario(usuario);
        contrato.setPropietario(propietario);
        contrato.setInquilino(inquilino);
        contrato.setPropiedad(propiedad);
        contrato.setLuz(luz);
        contrato.setGas(gas);
        contrato.setAgua(agua);
        contrato.setMunicipal(municipal);
        contrato.setGarantes(garantes);
//        contrato.setPdfContrato(pdfContrato);
    }


    @Override
    public List<ContratoSalidaDto> buscarContratoPorUsuario(String username) {

        List<Contrato> contratoList = contratoRepository.findContratosByUsername(username);
        return contratoList.stream()
                .map(contrato -> {
                    Long tiempoRestante = null;
                    try {
                        tiempoRestante = verificarFinalizacionContrato(contrato.getId_contrato());
                    } catch (ResourceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    contrato.setTiempoRestante(tiempoRestante);
                    return modelMapper.map(contrato, ContratoSalidaDto.class);
                })
                .toList();
    }

    @Override
    @Transactional
    public ContratoSalidaDto guardarContratoPdf(Long contratoId, ContratoModificacionDto actualizacion) throws ResourceNotFoundException {

        // Buscar el contrato por ID
        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + contratoId));

        // Actualizar el campo pdfContratoTexto si está presente
        if (actualizacion.getPdfContratoTexto() != null) {
            contrato.setPdfContratoTexto(actualizacion.getPdfContratoTexto());
        }

        // Guardar el contrato actualizado
        Contrato contratoActualizado = contratoRepository.save(contrato);

        // Convertir a ContratoSalidaDto usando ModelMapper o el método que prefieras
        return modelMapper.map(contratoActualizado, ContratoSalidaDto.class);
    }



    @Override
    public ContratoSalidaDto buscarContratoPorId(Long id){
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado"));
        return modelMapper.map(contrato, ContratoSalidaDto.class);
    }

    @Override
    public void eliminarContrato(Long id) throws ResourceNotFoundException{
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con el id: " + id));

        if (contrato.isActivo()) {
            throw new IllegalStateException("No se puede eliminar un contrato activo");
        }

        contratoRepository.deleteById(id);
        System.out.println("Contrato eliminado: " + contrato);

    }

    @Override
    public Boolean cambiarEstadoContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Contrato no encontrado"));
        contrato.setActivo(!contrato.isActivo());
        contratoRepository.save(contrato);
        return contrato.isActivo();
    }

    @Override
    public void finalizarContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        contrato.setActivo(false);
        contratoRepository.save(contrato);

        Propiedad propiedad = contrato.getPropiedad();
        propiedad.setDisponibilidad(true);
        propiedadRepository.save(propiedad);
    }

    @Override
    public void verificarActualizacionContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
        LocalDate fechaInicio = contrato.getFecha_inicio();
        int peridoActualizacion = contrato.getActualizacion();

        LocalDate ahora = LocalDate.now();
        long mesesTranscurridos = ChronoUnit.MONTHS.between(fechaInicio, ahora);

        if(mesesTranscurridos % peridoActualizacion == (peridoActualizacion - 1)){
            System.out.println("El contrato está por ser actualizado. Falta menos de un mes.");
        }

    }

    @Override
    public Long verificarFinalizacionContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        LocalDate fechaFin = contrato.getFecha_fin();
        LocalDate ahora = LocalDate.now();
        Long mensaje = 0L;
        long diasFaltantes = ChronoUnit.DAYS.between(ahora, fechaFin);
        if (diasFaltantes > 0) {
            mensaje= diasFaltantes;
        }
        else {
            finalizarContrato(id);
        }
        System.out.println(mensaje);
        return mensaje;
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void verificarAlertasContratos() {
        List<Contrato> contratos = contratoRepository.findAll();

        for(Contrato contrato : contratos) {
            try{
                verificarActualizacionContrato(contrato.getId_contrato());
                verificarFinalizacionContrato(contrato.getId_contrato());
            }catch (ResourceNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<LatestContratosSalidaDto> getLatestContratos() {
        List<Contrato> contratos = contratoRepository.findLatestContratos(PageRequest.of(0, 4)).getContent();
        LOGGER.info("Se obtuvieron los últimos 4 contratos");

        return contratos.stream()
                .map(contrato -> {
                    LatestContratosSalidaDto lts = new LatestContratosSalidaDto();
                    lts.setId(contrato.getId_contrato());
                    lts.setNombreContrato(contrato.getNombreContrato());
                    return lts;
                })
                .collect(Collectors.toList());
    }



}

