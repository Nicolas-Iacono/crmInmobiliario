package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ContratoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoActualizacionDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaSinGaranteDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.LatestContratosSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
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
import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentBag;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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


        modelMapper.typeMap(ContratoEntradaDto.class, Contrato.class)
                .addMapping(ContratoEntradaDto::getId_inquilino, Contrato::setInquilino)
                .addMapping(ContratoEntradaDto::getId_propiedad, Contrato::setPropiedad)
                .addMapping(ContratoEntradaDto::getId_propietario, Contrato::setPropietario);


        modelMapper.typeMap(Contrato.class, ContratoSalidaDto.class)
                .addMapping(Contrato::getInquilino, ContratoSalidaDto::setInquilino)
                .addMapping(Contrato::getPropiedad, ContratoSalidaDto::setPropiedad)
                .addMapping(Contrato::getPropietario, ContratoSalidaDto::setPropietario)
                .addMapping(Contrato::getGarantes, ContratoSalidaDto::setGarantes)
                .addMapping(Contrato::getRecibos,ContratoSalidaDto::setRecibos)
                .addMapping(Contrato::getTiempoRestante, ContratoSalidaDto::setTiempoRestante);

        modelMapper.typeMap(Contrato.class, ContratoSalidaSinGaranteDto.class)
                .addMapping(Contrato::getInquilino, ContratoSalidaSinGaranteDto::setInquilino)
                .addMapping(Contrato::getPropiedad, ContratoSalidaSinGaranteDto::setPropiedad)
                .addMapping(Contrato::getPropietario, ContratoSalidaSinGaranteDto::setPropietario)
                .addMapping(Contrato::getTiempoRestante, ContratoSalidaSinGaranteDto::setTiempoRestante)
                .addMapping(Contrato::getRecibos, ContratoSalidaSinGaranteDto::setRecibos);

        modelMapper.typeMap(Contrato.class, LatestContratosSalidaDto.class)
                .addMapping(Contrato::getUsuario, LatestContratosSalidaDto::setUsuarioDtoSalida);

        modelMapper.typeMap(ContratoModificacionDto.class, ContratoSalidaDto.class)
                .addMapping(ContratoModificacionDto::getPdfContratoTexto, ContratoSalidaDto::setContratoPdf)
                .addMapping(ContratoModificacionDto::getMontoAlquiler, ContratoSalidaDto::setMontoAlquiler);

    }


    @Override
    @Transactional
    public Integer enumerarContratos(String username) {
        return contratoRepository.countByUsuarioUsername(username);
    }

    @Override
    @Transactional
    public ContratoSalidaDto actualizarMontoAlquiler(ContratoModificacionDto contratoModificacionDto) throws ResourceNotFoundException {
        // Buscar el contrato
        Contrato contratoBuscado = contratoRepository.findById(contratoModificacionDto.getIdContrato())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        // Validar el nuevo monto
        Double nuevoMonto = contratoModificacionDto.getMontoAlquiler();
        if (nuevoMonto == null || nuevoMonto <= 0 ){
            throw new IllegalArgumentException("El nuevo monto de alquiler debe ser mayor que cero");
        }

        // Actualizar el monto
        contratoBuscado.setMontoAlquiler(nuevoMonto);

        // Guardar los cambios
        Contrato contratoActualizado = contratoRepository.save(contratoBuscado);

        // Crear manualmente el DTO de salida
        ContratoSalidaDto dto = new ContratoSalidaDto();
        dto.setId(contratoActualizado.getId_contrato());
        dto.setMontoAlquiler(contratoActualizado.getMontoAlquiler());

        return dto;
    }


    @Override
    @Transactional
    public List<ContratoSalidaDto> listarContratos() {
        List<Contrato> contratos = contratoRepository.findAll();

        return contratos.stream()
                .map(contrato -> {
                    // 🔄 Inicializamos las colecciones que vienen como Lazy
                    Hibernate.initialize(contrato.getGarantes());
                    Hibernate.initialize(contrato.getRecibos());
                    Hibernate.initialize(contrato.getPropietario());


                    for (Recibo recibo : contrato.getRecibos()) {
                        Hibernate.initialize(recibo.getImpuestos());
                    }

                    // ⏳ Cálculo de tiempo restante (fuera del mapeo DTO)
                    Long tiempoRestante;
                    try {
                        tiempoRestante = verificarFinalizacionContrato(contrato.getId_contrato());
                    } catch (ResourceNotFoundException e) {
                        throw new RuntimeException("No se pudo calcular el tiempo restante del contrato", e);
                    }

                    contrato.setTiempoRestante(tiempoRestante);

                    // 🔁 Mapeo del contrato sin garantes (los salteamos en el config)
                    ContratoSalidaDto contratoDto = modelMapper.map(contrato, ContratoSalidaDto.class);

                    // ✅ Mapeo manual de garantes para evitar ciclo
                    if (contrato.getGarantes() != null) {
                        List<GaranteSalidaDto> garantesDto = contrato.getGarantes().stream()
                                .map(garante -> modelMapper.map(garante, GaranteSalidaDto.class))
                                .collect(Collectors.toList());

                        contratoDto.setGarantes(garantesDto);
                    }

                    return contratoDto;
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


        List<Garante> garantes = obtenerGarantesPorIds(contratoEntradaDto.getGarantesIds());

        validarInquilinoYPropiedadDisponibles(inquilino, propiedad);

        // Crear el contrato y mapear sus datos
        Contrato contratoEnCreacion = modelMapper.map(contratoEntradaDto, Contrato.class);

        asignarEntidadesRelacionadas(contratoEntradaDto, contratoEnCreacion, usuario, propietario, inquilino, propiedad, garantes);

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
    @Transactional
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
    @Transactional
    private List<Garante> obtenerGarantesPorIds(List<Long> garantesIds) throws ResourceNotFoundException {
        List<Garante> garantes = new ArrayList<>();
        if (garantesIds == null || garantesIds.isEmpty()) {
            // Log opcional para debugging
            LOGGER.info("Contrato sin garantes asociados.");
            return garantes;
        }

        for (Long id : garantesIds) {
            Garante garante = garanteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró el garante con el id " + id));
            garantes.add(garante);
        }

        return garantes;
    }
    @Transactional
    private void validarInquilinoYPropiedadDisponibles(Inquilino inquilino, Propiedad propiedad) {
        Optional<Contrato> contratoExistente = contratoRepository.findByInquilinoAndActivoTrue(inquilino);
        if (contratoExistente.isPresent()) {
            throw new RuntimeException("El inquilino ya tiene un contrato activo y no puede ser asignado a otro contrato");
        }
        if (!propiedad.isDisponibilidad()) {
            throw new RuntimeException("La propiedad está asignada a otro contrato");
        }
    }

    private void asignarEntidadesRelacionadas(ContratoEntradaDto contratoEntradaDto,
                                              Contrato contrato,
                                              Usuario usuario,
                                              Propietario propietario,
                                              Inquilino inquilino,
                                              Propiedad propiedad,
                                              List<Garante> garantes) {

        contrato.setUsuario(usuario);
        contrato.setPropietario(propietario);
        contrato.setInquilino(inquilino);
        contrato.setPropiedad(propiedad);

        // Verificamos si hay garantes antes de asignar
        if (garantes != null && !garantes.isEmpty()) {
            for (Garante garante : garantes) {
                garante.setContrato(contrato); // Asignamos el contrato al garante
                LOGGER.info("Asignando garante id {} al contrato", garante.getId());
            }
            contrato.setGarantes(garantes);
        } else {
            contrato.setGarantes(Collections.emptyList()); // o null, según tu diseño de entidad
            LOGGER.info("Este contrato no tiene garantes asignados.");
        }

        contrato.setAguaEmpresa(contratoEntradaDto.getAguaEmpresa());
        contrato.setGasEmpresa(contratoEntradaDto.getGasEmpresa());
        contrato.setLuzEmpresa(contratoEntradaDto.getLuzEmpresa());
        contrato.setMunicipalEmpresa(contratoEntradaDto.getMunicipalEmpresa());
        contrato.setAguaPorcentaje(contratoEntradaDto.getAguaPorcentaje());
        contrato.setLuzPorcentaje(contratoEntradaDto.getLuzPorcentaje());
        contrato.setMunicipalPorcentaje(contratoEntradaDto.getMunicipalPorcentaje());
        contrato.setGasPorcentaje(contratoEntradaDto.getGasPorcentaje());
    }


    @Override
    @Transactional
    public List<ContratoSalidaDto> buscarContratoPorUsuario(String username) {

        List<Contrato> contratoList = contratoRepository.findContratosByUsername(username);
        return contratoList.stream()
                .map(contrato -> {
                    if (contrato.getGarantes() != null) {
                        contrato.setGarantes(new ArrayList<>(contrato.getGarantes()));
                    }
                    if (contrato.getRecibos() != null){
                        Hibernate.initialize(contrato.getRecibos());
                    }
                    for (Recibo recibo : contrato.getRecibos()) {
                        Hibernate.initialize(recibo.getImpuestos()); // 💥 ESTE es el que te falta
                    }
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
    @Transactional
    public ContratoSalidaDto buscarContratoPorId(Long id) {
        Contrato contrato = contratoRepository.findContratoByIdWithGarantes(id);
        if (contrato == null) {
            throw new EntityNotFoundException("Contrato no encontrado");
        }
        if (contrato.getGarantes() != null) {
            contrato.setGarantes(new ArrayList<>(contrato.getGarantes()));
        }
        if (contrato.getRecibos() != null){
            Hibernate.initialize(contrato.getRecibos());
        }
        for (Recibo recibo : contrato.getRecibos()) {
            Hibernate.initialize(recibo.getImpuestos());
        }
        return modelMapper.map(contrato, ContratoSalidaDto.class);
    }

    @Transactional
    @Override
    public void eliminarContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con el id: " + id));

        if (contrato.isActivo()) {
            throw new IllegalStateException("No se puede eliminar un contrato activo");
        }

        // Desvincular garantes si existen
        List<Garante> garantes = contrato.getGarantes();
        if (garantes != null && !garantes.isEmpty()) {
            for (Garante garante : garantes) {
                garante.setContrato(null);
                garanteRepository.save(garante);
            }
        }
        contratoRepository.delete(contrato);

    }
    @Transactional
    @Override
    public Boolean cambiarEstadoContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Contrato no encontrado"));
        contrato.setActivo(!contrato.isActivo());
        contratoRepository.save(contrato);
        return contrato.isActivo();
    }
    @Transactional
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
    @Transactional
    @Override
    public ContratoActualizacionDtoSalida verificarActualizacionContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
        System.out.println("contrato: " + contrato.getId_contrato());

        LocalDate fechaInicio = contrato.getFecha_inicio();
        System.out.println("Fecha inicio: " + fechaInicio);
        if (fechaInicio == null) {
            return new ContratoActualizacionDtoSalida(null, 0, 0, false, "❌ El contrato no tiene fecha de inicio asignada");
        }
        int periodoActualizacion = contrato.getActualizacion(); // ej: cada 6 meses

        LocalDate ahora = LocalDate.now();
        long mesesTranscurridos = ChronoUnit.MONTHS.between(fechaInicio, ahora);
        long periodosTranscurridos = mesesTranscurridos / periodoActualizacion;

        LocalDate proximaActualizacion = fechaInicio.plusMonths((periodosTranscurridos + 1) * periodoActualizacion);
        System.out.println("Próxima actualización: " + proximaActualizacion);
        if (!proximaActualizacion.isAfter(ahora)) {
            return new ContratoActualizacionDtoSalida(
                    proximaActualizacion,
                    0,
                    0,
                    true,
                    "⚠️ ¡El contrato ya debería haberse actualizado!"
            );
        }

        Period diferencia = Period.between(ahora, proximaActualizacion);
        System.out.println("Meses restantes: " + diferencia.getMonths());

        return new ContratoActualizacionDtoSalida(
                proximaActualizacion,
                diferencia.getMonths(),
                diferencia.getDays(),
                false,
                "📅 Contrato pendiente de actualización"
        );
    }
    @Transactional
    @Override
    public Long verificarFinalizacionContrato(Long id) throws ResourceNotFoundException {
        return null;
    }
    @Transactional
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
    @Transactional
    @Override
    public List<LatestContratosSalidaDto> getLatestContratos() {
        List<Contrato> contratos = contratoRepository.findLatestContratos(PageRequest.of(0, 4)).getContent();
        LOGGER.info("Se obtuvieron los últimos 4 contratos");

        return contratos.stream()
                .map(contrato -> {
                    LatestContratosSalidaDto lts = new LatestContratosSalidaDto();
                    lts.setId(contrato.getId_contrato());
                    lts.setNombreContrato(contrato.getNombreContrato());
                    modelMapper.map(contrato, LatestContratosSalidaDto.class);
                    lts.setUsuarioDtoSalida(modelMapper.map(contrato.getUsuario(), UsuarioDtoSalida.class));
                    return lts;
                })
                .collect(Collectors.toList());
    }




}

