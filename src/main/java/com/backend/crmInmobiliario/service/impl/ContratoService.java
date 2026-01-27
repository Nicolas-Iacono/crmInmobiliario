package com.backend.crmInmobiliario.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.crmInmobiliario.DTO.ContratoCreadoEvent;
import com.backend.crmInmobiliario.DTO.entrada.contrato.*;
//import com.backend.crmInmobiliario.DTO.entrada.planesYSuscripcion.PlanLimitsDto;
import com.backend.crmInmobiliario.DTO.entrada.inquilino.InquilinoContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.entrada.usuarioInquilino.InquilinoDtoSalida;
import com.backend.crmInmobiliario.DTO.entrada.usuarioPropietario.PropietarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.PropiedadContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropietarioContratoDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoComisionDtoSalida;
import com.backend.crmInmobiliario.DTO.modificacion.ContratoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.*;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Subscription;
import com.backend.crmInmobiliario.exception.ContractLimitExceededException;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.*;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.SubscriptionRepository;
import com.backend.crmInmobiliario.service.IContratoService;
import com.backend.crmInmobiliario.service.impl.IA.EmbeddingService;
import com.backend.crmInmobiliario.service.impl.notificacionesPush.PushNotificationService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.backend.crmInmobiliario.utils.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import okhttp3.*;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ContratoService implements IContratoService {

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SUPABASE_ANON_KEY;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    @Value("${openai.api.key}")
    private String OPENAI_APIKEY;

    @Value("${app.contracts.trial-limit:3}")
    private int trialLimit;
    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);
    private final ApplicationEventPublisher publisher;
    private static final int MAX_EMBEDDING_CHARS = 12_000;
    private final ApplicationEventPublisher eventPublisher;
    private static boolean has(Object v) { return v != null && !v.toString().trim().isEmpty(); }
    static String opt(Object v) { return v == null ? "" : v.toString().trim(); }
    private static void kv(StringBuilder sb, String key, Object val) { if (has(val)) sb.append(" | ").append(key).append("=").append(opt(val)); }
    private static String cap(String s) { return s.length() > MAX_EMBEDDING_CHARS ? s.substring(0, MAX_EMBEDDING_CHARS) : s; }
//    private  SubscriptionService subscriptionService;
    private ContratoRepository contratoRepository;
    private ModelMapper modelMapper;

    private InquilinoRepository inquilinoRepository;

    private PropietarioRepository propietarioRepository;

    private PropiedadRepository propiedadRepository;

    private IngresoMensualService ingresoMensualService;

    private GaranteRepository garanteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    private SubscriptionRepository subscriptionRepository;
    private PlanRepository planRepository;
    private GasRepository gasRepository;
    private AguaRepository aguaRepository;
    private LuzRepository luzRepository;
    private MunicipalRepository municipalRepository;
    private NotaRepository notaRepository;
    private ReciboRepository reciboRepository;
    private ImpuestoRepository impuestoRepository;
    private ContratoAlertaRepository contratoAlertaRepository;
    private PushSubscriptionRepository pushSubscriptionRepository;
    private PushNotificationService pushNotificationService;
    private AuthUtil authUtil;
    private EmbeddingService embeddingService;
    private IngresoMensualRepository ingresoMensualRepository;
    private final GaranteService garanteService;


    //    private PdfContratoRepository pdfContratoRepository;
    private JwtUtil jwtUtil;
    public ContratoService(ApplicationEventPublisher publisher, ApplicationEventPublisher eventPublisher, IngresoMensualRepository ingresoMensualRepository,
                           EmbeddingService embeddingService,
                           AuthUtil authUtil,
                           JwtUtil jwtUtil,
                           ImpuestoRepository impuestoRepository,
                           ContratoRepository contratoRepository,
                           SubscriptionRepository subscriptionRepository,
                           PlanRepository planRepository,
                           GaranteRepository garanteRepository,
                           ModelMapper modelMapper,
                           InquilinoRepository inquilinoRepository,
                           PropietarioRepository propietarioRepository,
                           PropiedadRepository propiedadRepository,
                           GasRepository gasRepository,
                           AguaRepository aguaRepository,
                           LuzRepository luzRepository,
                           MunicipalRepository municipalRepository,
                           NotaRepository notaRepository,
                           ReciboRepository reciboRepository,
//                         SubscriptionService subscriptionService
//                         UsuarioRepository usuarioRepository,
                           IngresoMensualService ingresoMensualService,
                           GaranteService garanteService,
                           ContratoAlertaRepository contratoAlertaRepository,
                           PushSubscriptionRepository pushSubscriptionRepository,
                           PushNotificationService pushNotificationService

    ) {
        this.publisher = publisher;
        this.eventPublisher = eventPublisher;
        this.authUtil = authUtil;
        this.jwtUtil=jwtUtil;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
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
        this.garanteService = garanteService;
        this.contratoAlertaRepository = contratoAlertaRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.pushNotificationService = pushNotificationService;
        this.usuarioRepository = usuarioRepository;
        this.notaRepository = notaRepository;
        this.reciboRepository = reciboRepository;
        this.impuestoRepository = impuestoRepository;
        this.ingresoMensualService = ingresoMensualService;
        this.embeddingService = embeddingService;
        this.ingresoMensualRepository = ingresoMensualRepository;
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
                .addMapping(Contrato::getEstados, ContratoSalidaDto::setEstados)
                .addMapping(Contrato::getTiempoRestante, ContratoSalidaDto::setTiempoRestante)
                .addMapping(src -> src.getEstado() != null ? src.getEstado().name() : null,
                        ContratoSalidaDto::setEstado);

        modelMapper.typeMap(Contrato.class, ContratoSalidaSinGaranteDto.class)
                .addMapping(Contrato::getInquilino, ContratoSalidaSinGaranteDto::setInquilino)
                .addMapping(Contrato::getPropiedad, ContratoSalidaSinGaranteDto::setPropiedad)
                .addMapping(Contrato::getPropietario, ContratoSalidaSinGaranteDto::setPropietario)
                .addMapping(Contrato::getTiempoRestante, ContratoSalidaSinGaranteDto::setTiempoRestante)
                .addMapping(Contrato::getEstados, ContratoSalidaSinGaranteDto::setEstados)
                .addMapping(Contrato::getRecibos, ContratoSalidaSinGaranteDto::setRecibos)
                .addMapping(src -> src.getEstado() != null ? src.getEstado().name() : null,
                        ContratoSalidaSinGaranteDto::setEstado);


        modelMapper.typeMap(Contrato.class, LatestContratosSalidaDto.class)
                .addMapping(Contrato::getEstados, LatestContratosSalidaDto::setEstados)
                .addMapping(Contrato::getUsuario, LatestContratosSalidaDto::setUsuarioDtoSalida);

        modelMapper.typeMap(ContratoModificacionDto.class, ContratoSalidaDto.class)
                .addMapping(ContratoModificacionDto::getEstados, ContratoSalidaDto::setEstados)
                .addMapping(ContratoModificacionDto::getPdfContratoTexto, ContratoSalidaDto::setContratoPdf)
                .addMapping(ContratoModificacionDto::getMontoAlquiler, ContratoSalidaDto::setMontoAlquiler)
                .addMapping(ContratoModificacionDto::getComisionMensualPorc, ContratoSalidaDto::setComisionMensualPorc)
                .addMapping(ContratoModificacionDto::getComisionContratoPorc, ContratoSalidaDto::setComisionContratoPorc);

        modelMapper.typeMap(Contrato.class, ContratoBasicoDto.class)
                .addMapping(Contrato::getPdfContratoTexto, ContratoBasicoDto::setContratoPdf)
                .addMapping(src -> src.getEstado() != null ? src.getEstado().name() : null,
                        ContratoBasicoDto::setEstado);

        modelMapper.typeMap(Inquilino.class, InquilinoContratoDtoSalida.class)
                .addMappings(mapper -> {
                    mapper.map(Inquilino::getId, InquilinoContratoDtoSalida::setId);
                    mapper.map(Inquilino::getPronombre, InquilinoContratoDtoSalida::setPronombre);
                    mapper.map(Inquilino::getNombre, InquilinoContratoDtoSalida::setNombre);
                    mapper.map(Inquilino::getApellido, InquilinoContratoDtoSalida::setApellido);
                    mapper.map(Inquilino::getTelefono, InquilinoContratoDtoSalida::setTelefono);
                    mapper.map(Inquilino::getEmail, InquilinoContratoDtoSalida::setEmail);
                    mapper.map(Inquilino::getDni, InquilinoContratoDtoSalida::setDni);
                    mapper.map(Inquilino::getDireccionResidencial, InquilinoContratoDtoSalida::setDireccionResidencial);
                    mapper.map(Inquilino::getCuit, InquilinoContratoDtoSalida::setCuit);
                    mapper.map(Inquilino::getNacionalidad, InquilinoContratoDtoSalida::setNacionalidad);
                    mapper.map(Inquilino::getEstadoCivil, InquilinoContratoDtoSalida::setEstadoCivil);
                });

        modelMapper.typeMap(Propietario.class, PropietarioContratoDtoSalida.class)
                .addMappings(mapper -> {
                    mapper.map(Propietario::getId, PropietarioContratoDtoSalida::setId);
                    mapper.map(Propietario::getPronombre, PropietarioContratoDtoSalida::setPronombre);
                    mapper.map(Propietario::getNombre, PropietarioContratoDtoSalida::setNombre);
                    mapper.map(Propietario::getApellido, PropietarioContratoDtoSalida::setApellido);
                    mapper.map(Propietario::getTelefono, PropietarioContratoDtoSalida::setTelefono);
                    mapper.map(Propietario::getEmail, PropietarioContratoDtoSalida::setEmail);
                    mapper.map(Propietario::getDni, PropietarioContratoDtoSalida::setDni);
                    mapper.map(Propietario::getDireccionResidencial, PropietarioContratoDtoSalida::setDireccionResidencial);
                    mapper.map(Propietario::getCuit, PropietarioContratoDtoSalida::setCuit);
                    mapper.map(Propietario::getNacionalidad, PropietarioContratoDtoSalida::setNacionalidad);
                    mapper.map(Propietario::getEstadoCivil, PropietarioContratoDtoSalida::setEstadoCivil);
                });

        modelMapper.typeMap(Garante.class, GaranteSalidaDto.class)
                .addMappings(mapper -> {
                    mapper.map(Garante::getId, GaranteSalidaDto::setId);
                    mapper.map(Garante::getNombre, GaranteSalidaDto::setNombre);
                    mapper.map(Garante::getApellido, GaranteSalidaDto::setApellido);
                    mapper.map(Garante::getDni, GaranteSalidaDto::setDni);
                    mapper.map(Garante::getCuit, GaranteSalidaDto::setCuit);
                    mapper.map(Garante::getDireccionResidencial, GaranteSalidaDto::setDireccionResidencial);
                    mapper.map(Garante::getTelefono, GaranteSalidaDto::setTelefono);
                    mapper.map(Garante::getTipoGarantia, GaranteSalidaDto::setTipoGarantia);
                });
    }




    @Override
    @Transactional
    public Integer enumerarContratos(String username) {
        return contratoRepository.countByUsuarioUsername(username);
    }

    @Transactional
    public ContratoComisionDtoSalida actualizarComisiones(ContratoComisionUpdateDto dto) {
        Contrato c = contratoRepository.findById(dto.getIdContrato())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        if (dto.getComisionContratoPorc() != null) {
            validarPorc(dto.getComisionContratoPorc());
            c.setComisionContratoPorc(dto.getComisionContratoPorc().setScale(2, RoundingMode.HALF_UP));
        }
        if (dto.getComisionMensualPorc() != null) {
            validarPorc(dto.getComisionMensualPorc());
            c.setComisionMensualPorc(dto.getComisionMensualPorc().setScale(2, RoundingMode.HALF_UP));
        }

        contratoRepository.save(c);

        // Armamos salida plana (sin entidades perezosas)
        ContratoComisionDtoSalida out = new ContratoComisionDtoSalida();
        out.setComisionContratoPorc(c.getComisionContratoPorc());
        out.setComisionMensualPorc(c.getComisionMensualPorc());
        out.setComisionContratoMonto(c.getComisionContratoMonto());               // @Transient en tu entidad
        out.setComisionMensualMonto(c.getComisionMensualMonto());                 // @Transient en tu entidad
        out.setMontoMensualPropietario(c.getLiquidacionPropietarioMensual());     // @Transient en tu entidad
        return out;
    }

    private void validarPorc(BigDecimal p) {
        if (p.compareTo(BigDecimal.ZERO) < 0 || p.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100");
        }
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

        java.math.BigDecimal alquiler   = java.math.BigDecimal.valueOf(contratoActualizado.getMontoAlquiler());
        java.math.BigDecimal meses      = java.math.BigDecimal.valueOf(contratoBuscado.getDuracion() != 0 ? contratoBuscado.getDuracion() : 0);
        java.math.BigDecimal pctContrato= pct(contratoActualizado.getComisionContratoPorc());   // helper de abajo
        java.math.BigDecimal pctMensual = pct(contratoActualizado.getComisionMensualPorc());    // helper de abajo

        java.math.BigDecimal comisionContratoMonto  = (contratoBuscado.getDuracion() > 0)
                ? alquiler.multiply(meses).multiply(pctContrato)
                : java.math.BigDecimal.ZERO;

        java.math.BigDecimal comisionMensualMonto   = alquiler.multiply(pctMensual);
        java.math.BigDecimal montoMensualPropietario= alquiler.subtract(comisionMensualMonto);

        ContratoSalidaDto dto = new ContratoSalidaDto();
        dto.setId(contratoActualizado.getId());
        dto.setMontoAlquiler(contratoActualizado.getMontoAlquiler());
        dto.setDuracion(contratoBuscado.getDuracion());

        // porcentajes actuales del contrato
        dto.setComisionContratoPorc(contratoActualizado.getComisionContratoPorc());
        dto.setComisionMensualPorc(contratoActualizado.getComisionMensualPorc());

        // montos recalculados
        dto.setComisionContratoMonto(comisionContratoMonto);
        dto.setComisionMensualMonto(comisionMensualMonto);
        dto.setMontoMensualPropietario(montoMensualPropietario);

        return dto;
    }


    @Transactional
    public void editarContrato(Long contratoId, ContratoModificacionDto dto)
            throws ResourceNotFoundException {

        if (!contratoRepository.existsById(contratoId)) {
            throw new ResourceNotFoundException("Contrato no encontrado");
        }
        // 1) Buscar contrato
        Contrato contrato = contratoRepository.getReferenceById(contratoId);

        // 2) Actualizar campos básicos
        if (dto.getNombreContrato() != null && !dto.getNombreContrato().isBlank()) {
            contrato.setNombreContrato(dto.getNombreContrato());
        }

//        if (dto.getFecha_inicio() != null) {
//            contrato.setFecha_inicio(dto.getFecha_inicio());
//        }
//
//        if (dto.getFecha_fin() != null) {
//            contrato.setFecha_fin(dto.getFecha_fin());
//        }

        if (dto.getMontoAlquiler() != null) {
            if (dto.getMontoAlquiler() <= 0) {
                throw new IllegalArgumentException("El monto de alquiler debe ser mayor que cero");
            }
            contrato.setMontoAlquiler(dto.getMontoAlquiler());
        }

        if (dto.getDuracion() != null && dto.getDuracion() > 0) {
            contrato.setDuracion(dto.getDuracion());
        }

        if (dto.getActualizacion() != null && dto.getActualizacion() > 0) {
            contrato.setActualizacion(dto.getActualizacion());
        }

        if (dto.getDestino() != null) {
            contrato.setDestino(dto.getDestino());
        }

        if (dto.getIndiceAjuste() != null) {
            contrato.setIndiceAjuste(dto.getIndiceAjuste());
        }

        if (dto.getMontoAlquilerLetras() != null) {
            contrato.setMontoAlquilerLetras(dto.getMontoAlquilerLetras());
        }

        if (dto.getMultaXDia() != null) {
            contrato.setMultaXDia(dto.getMultaXDia());
        }

        if (dto.getTipoGarantia() != null) {
            contrato.setTipoGarantia(dto.getTipoGarantia());
        }

        // 3) Servicios
        if (dto.getAguaEmpresa() != null) contrato.setAguaEmpresa(dto.getAguaEmpresa());
        if (dto.getGasEmpresa() != null) contrato.setGasEmpresa(dto.getGasEmpresa());
        if (dto.getLuzEmpresa() != null) contrato.setLuzEmpresa(dto.getLuzEmpresa());
        if (dto.getMunicipalEmpresa() != null) contrato.setMunicipalEmpresa(dto.getMunicipalEmpresa());

        if (dto.getAguaPorcentaje() != null) contrato.setAguaPorcentaje(dto.getAguaPorcentaje());
        if (dto.getGasPorcentaje() != null) contrato.setGasPorcentaje(dto.getGasPorcentaje());
        if (dto.getLuzPorcentaje() != null) contrato.setLuzPorcentaje(dto.getLuzPorcentaje());
        if (dto.getMunicipalPorcentaje() != null) contrato.setMunicipalPorcentaje(dto.getMunicipalPorcentaje());

        // 4) Comisiones
        if (dto.getComisionContratoPorc() != null) {
            validarPorc(dto.getComisionContratoPorc());
            contrato.setComisionContratoPorc(dto.getComisionContratoPorc().setScale(2, RoundingMode.HALF_UP));
        }

        if (dto.getComisionMensualPorc() != null) {
            validarPorc(dto.getComisionMensualPorc());
            contrato.setComisionMensualPorc(dto.getComisionMensualPorc().setScale(2, RoundingMode.HALF_UP));
        }

        // 5) Texto del contrato
        if (dto.getPdfContratoTexto() != null) {
            contrato.setPdfContratoTexto(dto.getPdfContratoTexto());
        }

        // (Opcional) si quisieras permitir cambiar propietario/inquilino/propiedad
        // tendrías que buscar las entidades y reasignarlas acá.
        // Yo de momento no lo toco para no romper nada.

        // 6) Guardar cambios en DB
        Contrato contratoActualizado = contratoRepository.save(contrato);

        // 7) Sincronizar Supabase (contratos + embeddings)
        publisher.publishEvent(new ContratoActualizadoEvent(contratoActualizado.getId()));

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
                        tiempoRestante = verificarFinalizacionContrato(contrato.getId());
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
    public Long verificarFinalizacionContrato(Long id) throws ResourceNotFoundException {
        Contrato c = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        LocalDate fin = c.getFecha_fin();
        if (fin == null) return null; // 🚫 NO recalcular nunca

        LocalDate hoy = LocalDate.now();
        if (!fin.isAfter(hoy)) return 0L;

        return ChronoUnit.DAYS.between(hoy, fin);
    }

//    public void ensureCanCreateOrActivate(Long usuarioId) {
//        PlanLimitsDto limits = subscriptionService.getLimits(usuarioId);
//        boolean ilimitado = limits.getContractLimit() == -1;
//
//        if (!ilimitado) {
//            long usados = contratoRepository.countActivosByUsuario(usuarioId);
//            int limit = limits.getContractLimit();
//
//            if (usados >= limit) {
//                String planName = limits.getPlanName();
//                throw new ContractLimitExceededException(String.format(
//                        "Alcanzaste tu límite de %d contratos del plan '%s'. " +
//                                "Actualizá tu suscripción para continuar.", limit, planName));
//            }
//        }
//    }



    @Transactional(noRollbackFor = ContractLimitExceededException.class)
    @Override
    public ContratoSalidaDto crearContrato(ContratoEntradaDto contratoEntradaDto)
            throws ResourceNotFoundException, IOException {

        validarContratoEntrada(contratoEntradaDto);

        Long idUser = authUtil.extractUserId();
        Usuario usuario = usuarioRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        validarLimiteDeContratos();

        Propietario propietario = propietarioRepository.findById(contratoEntradaDto.getId_propietario())
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

        Inquilino inquilino = inquilinoRepository.findById(contratoEntradaDto.getId_inquilino())
                .orElseThrow(() -> new ResourceNotFoundException("Inquilino no encontrado"));

        Propiedad propiedad = propiedadRepository.findById(contratoEntradaDto.getId_propiedad())
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        // 🔹 Garantes (si no es seguro caución)
        List<Garante> garantes = new ArrayList<>();

        String garantia = Optional.ofNullable(contratoEntradaDto.getTipoGarantia()).orElse("");
        String normalized = garantia.trim()
                .replace("de", "")
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z_]", "")
                .toUpperCase();

        boolean esSeguroCaucion = normalized.equals("SEGURO_CAUCION");

        if (!esSeguroCaucion) {
            garantes = obtenerGarantesPorIds(contratoEntradaDto.getGarantesIds());
            if (garantes == null || garantes.isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar al menos un garante o usar seguro de caución");
            }
        }

        validarInquilinoYPropiedadDisponibles(inquilino, propiedad);

        if (contratoEntradaDto.getFecha_inicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (contratoEntradaDto.getDuracion() <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a cero");
        }

        LocalDate fechaInicio = contratoEntradaDto.getFecha_inicio();
        LocalDate fechaFin = contratoEntradaDto.getFecha_fin();
        if (fechaFin == null) {
            fechaFin = fechaInicio.plusMonths(contratoEntradaDto.getDuracion());
        }

        // Crear contrato
        Contrato contratoEnCreacion = modelMapper.map(contratoEntradaDto, Contrato.class);
        contratoEnCreacion.setFecha_inicio(fechaInicio);
        contratoEnCreacion.setFecha_fin(fechaFin);

        contratoEnCreacion.setTipoGarantia(contratoEntradaDto.getTipoGarantia());
        contratoEnCreacion.setMontoAlquiler(contratoEntradaDto.getMontoAlquiler());
        contratoEnCreacion.setMultaXDia(contratoEntradaDto.getMultaXDia());
        contratoEnCreacion.setMontoAlquilerLetras(contratoEntradaDto.getMontoAlquilerLetras());
        contratoEnCreacion.setActualizacion(contratoEntradaDto.getActualizacion());
        contratoEnCreacion.setDuracion(contratoEntradaDto.getDuracion());
        contratoEnCreacion.setDestino(contratoEntradaDto.getDestino());
        contratoEnCreacion.setIndiceAjuste(contratoEntradaDto.getIndiceAjuste());

        contratoEnCreacion.setComisionContratoPorc(
                contratoEntradaDto.getComisionContratoPorc() != null ? contratoEntradaDto.getComisionContratoPorc() : BigDecimal.ZERO
        );
        contratoEnCreacion.setComisionMensualPorc(
                contratoEntradaDto.getComisionMensualPorc() != null ? contratoEntradaDto.getComisionMensualPorc() : BigDecimal.ZERO
        );

        asignarEntidadesRelacionadas(
                contratoEntradaDto,
                contratoEnCreacion,
                usuario,
                propietario,
                inquilino,
                propiedad,
                garantes
        );
        contratoEnCreacion.setActivo(true);
        contratoEnCreacion.setEstado(EstadoContrato.ACTIVO);

        // ✅ Persistir en SQL
        Contrato contratoPersistido = contratoRepository.save(contratoEnCreacion);
        contratoPersistido.getEstados().add(EstadoContrato.ACTIVO);
        // Estado inquilino
        inquilino.setActivo(true);
        inquilinoRepository.save(inquilino);

        // Propiedad no disponible
        propiedad.setDisponibilidad(false);
        propiedadRepository.save(propiedad);

        // Generar ingresos
        ingresoMensualService.generarParaContrato(contratoPersistido);

        // ✅ Disparar sync a Supabase/Embeddings DESPUÉS del commit
        eventPublisher.publishEvent(new ContratoCreadoEvent(contratoPersistido.getId()));

        return modelMapper.map(contratoPersistido, ContratoSalidaDto.class);
    }




    private static <T> List<Long> safeIds(List<? extends T> list) {
        if (list == null || list.isEmpty()) return List.of();
        return list.stream()
                .map(o -> {
                    if (o instanceof Recibo r)     return r.getId();
                    if (o instanceof Nota n)       return n.getId();
                    if (o instanceof Garante g)    return g.getId();
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public String buildContenidoContrato(Contrato c) {
        StringBuilder sb = new StringBuilder();

        // ===== Resumen contrato =====
        sb.append("CONTRATO|")
                .append("id=").append(c.getId())
                .append(" | nombre=").append(opt(c.getNombreContrato()))
                .append(" | activo=").append(c.isActivo() ? "si" : "no")
                .append(" | destino=").append(opt(c.getDestino()))
                .append(" | montoAlquiler=").append(opt(c.getMontoAlquiler()))
                .append(" | duracionMeses=").append(opt(c.getDuracion()))
                .append(" | actualizacionMeses=").append(opt(c.getActualizacion()))
                .append(" | inicio=").append(opt(c.getFecha_inicio()))
                .append(" | fin=").append(opt(c.getFecha_fin()))
                .append(" | usuario_id=").append(opt(c.getUsuario().getId()))
                .append(" | propietario_id=").append(opt(c.getPropietario().getId()))
                .append(" | inquilino_id=").append(opt(c.getInquilino().getId()))
                .append(" | propiedad_id=").append(opt(c.getPropiedad().getId_propiedad()))
                .append(" | garantes_id=").append(
                        Optional.ofNullable(c.getGarantes()).orElseGet(List::of).stream()
                                .map(g -> String.valueOf(g.getId()))
                                .collect(Collectors.joining(",")))
                .append(" | recibos_id=").append(
                        Optional.ofNullable(c.getRecibos()).orElseGet(List::of).stream()
                                .map(g -> String.valueOf(g.getId()))
                                .collect(Collectors.joining(",")))
                .append(" | notas_id=").append(
                        Optional.ofNullable(c.getNotas()).orElseGet(List::of).stream()
                                .map(g -> String.valueOf(g.getId()))
                                .collect(Collectors.joining(",")))
                .append('\n');

        // ===== Propiedad =====
        if (c.getPropiedad() != null) {
            var p = c.getPropiedad();
            StringBuilder line = new StringBuilder("PROPIEDAD|");
            kv(line, "id", p.getId_propiedad());
            kv(line, "tipo", p.getTipo());
            kv(line, "direccion", p.getDireccion());
            kv(line, "localidad", p.getLocalidad());
            kv(line, "partido", p.getPartido());
            kv(line, "provincia", p.getProvincia());
            kv(line, "inventario", p.getInventario());
            line.append(" | disponible=").append(p.isDisponibilidad() ? "si" : "no");
            sb.append(line).append('\n');
        }

        // ===== Inquilino detallado =====
        if (c.getInquilino() != null) {
            var i = c.getInquilino();
            StringBuilder line = new StringBuilder("INQUILINO|");
            kv(line, "nombre", i.getNombre());
            kv(line, "apellido", i.getApellido());
            kv(line, "pronombre", i.getPronombre());
            kv(line, "nacionalidad", i.getNacionalidad());
            kv(line, "estado_civil", i.getEstadoCivil());
            kv(line, "direccion_residencial", i.getDireccionResidencial());
            kv(line, "telefono", i.getTelefono());
            kv(line, "email", i.getEmail());
            // PII opcional:
            // kv(line, "dni", i.getDni()); kv(line, "cuit", i.getCuit());
            sb.append(line).append('\n');
        }

        // ===== Propietario detallado =====
        if (c.getPropietario() != null) {
            var pr = c.getPropietario();
            StringBuilder line = new StringBuilder("PROPIETARIO|");
            kv(line, "nombre", pr.getNombre());
            kv(line, "apellido", pr.getApellido());
            kv(line, "pronombre", pr.getPronombre());
            kv(line, "nacionalidad", pr.getNacionalidad());
            kv(line, "estado_civil", pr.getEstadoCivil());
            kv(line, "direccion_residencial", pr.getDireccionResidencial());
            kv(line, "telefono", pr.getTelefono());
            kv(line, "email", pr.getEmail());
            // PII opcional:
            // kv(line, "dni", pr.getDni()); kv(line, "cuit", pr.getCuit());
            sb.append(line).append('\n');
        }

        // ===== Servicios contractuales =====
        sb.append("SERVICIOS|")
                .append("agua=").append(opt(c.getAguaEmpresa())).append(" ").append(opt(c.getAguaPorcentaje())).append("%")
                .append(" | gas=").append(opt(c.getGasEmpresa())).append(" ").append(opt(c.getGasPorcentaje())).append("%")
                .append(" | luz=").append(opt(c.getLuzEmpresa())).append(" ").append(opt(c.getLuzPorcentaje())).append("%")
                .append(" | municipal=").append(opt(c.getMunicipalEmpresa())).append(" ").append(opt(c.getMunicipalPorcentaje())).append("%")
                .append('\n');

        // ===== Garantes detallados =====
        if (c.getGarantes() != null && !c.getGarantes().isEmpty()) {
            for (var g : c.getGarantes()) {
                StringBuilder line = new StringBuilder("GARANTE|");
                kv(line, "id", g.getId());
                kv(line, "nombre", g.getNombre());
                kv(line, "apellido", g.getApellido());
                kv(line, "pronombre", g.getPronombre());
                kv(line, "nacionalidad", g.getNacionalidad());
                kv(line, "telefono", g.getTelefono());
                kv(line, "email", g.getEmail());
                kv(line, "direccion", g.getDireccion());
                // Laboral/empresa
                kv(line, "cargo_actual", g.getCargoActual());
                kv(line, "sector_actual", g.getSectorActual());
                kv(line, "nombre_empresa", g.getNombreEmpresa());
                kv(line, "cuit_empresa", g.getCuitEmpresa());
                kv(line, "legajo", g.getLegajo());
                // Garantía / inmueble
                kv(line, "tipo_garantia", g.getTipoGarantia());
                kv(line, "tipo_propiedad", g.getTipoPropiedad());
                kv(line, "partida_inmobiliaria", g.getPartidaInmobiliaria());
                kv(line, "info_catastral", g.getInfoCatastral());
                kv(line, "estado_ocupacion", g.getEstadoOcupacion());
                // Informes
                kv(line, "informe_dominio", g.getInformeDominio());
                kv(line, "informe_inhibicion", g.getInformeInhibicion());
                // PII opcional:
                // kv(line, "dni", g.getDni()); kv(line, "cuit", g.getCuit());
                sb.append(line).append('\n');
            }
        }

        // ===== Recibos + Impuestos detallados (fuera del bloque de garantes) =====
        if (c.getRecibos() != null && !c.getRecibos().isEmpty()) {
            sb.append("RECIBOS_TOTAL=").append(c.getRecibos().size()).append('\n');

            String recibosTxt = c.getRecibos().stream()
                    .limit(10)
                    .map(r -> {
                        String impuestosTxt =
                                (r.getImpuestos() == null || r.getImpuestos().isEmpty())
                                        ? "sin_impuestos"
                                        : r.getImpuestos().stream()
                                        .map(i -> "{" +
                                                "empresa=" + opt(i.getEmpresa()) +
                                                ", tipo=" + opt(i.getTipoImpuesto()) +
                                                ", porcentaje=" + opt(i.getPorcentaje()) + "%" +
                                                ", montoAPagar=" + opt(i.getMontoAPagar()) +
                                                ", fechaFactura=" + opt(i.getFechaFactura()) +
                                                // si tu getter es isEstadoPago() o getEstadoPago(), opt(...) lo maneja
                                                ", estadoPago=" + (has(i) ? opt(i.isEstadoPago()) : "") +
                                                "}")
                                        .collect(Collectors.joining(", "));

                        return "RECIBO|" +
                                "id=" + opt(r.getId()) +
                                " | numero=" + opt(r.getNumeroRecibo()) +
                                " | periodo=" + opt(r.getPeriodo()) +
                                " | montoTotal=" + opt(r.getMontoTotal()) +
                                " | emision=" + opt(r.getFechaEmision()) +
                                " | vencimiento=" + opt(r.getFechaVencimiento()) +
                                " | concepto=" + opt(r.getConcepto()) +
                                " | estado=" + opt(r.getEstado()) +
                                " | impuestos=[" + impuestosTxt + "]";
                    })
                    .collect(Collectors.joining("\n"));

            sb.append(recibosTxt).append('\n');
        }

        // ===== Notas =====
        if (c.getNotas() != null && !c.getNotas().isEmpty()) {
            String notasTxt = c.getNotas().stream()
                    .limit(10)
                    .map(n -> "NOTA|" +
                            "id=" + opt(n.getId()) +           // si tu entidad es getId_nota(), cambiá por getId_nota()
                            " | tipo=" + opt(n.getTipo()) +
                            " | fecha=" + opt(n.getFechaCreacion()) +
                            " | estado=" + opt(n.getEstado()) +
                            " | prioridad=" + opt(n.getPrioridad()) +
                            " | motivo=" + opt(n.getMotivo()) +
                            " | contenido=" + opt(n.getContenido()) +
                            " | observaciones=" + opt(n.getObservaciones()))
                    .collect(Collectors.joining("\n"));
            sb.append(notasTxt).append('\n');
        }

        return cap(sb.toString());
    }


    private String buildContenidoContratoPlano(Contrato c) {
        StringBuilder sb = new StringBuilder();

        // ===== CONTRATO =====
        sb.append("CONTRATO|")
                .append("id=").append(c.getId())
                .append(" | nombre=").append(opt(c.getNombreContrato()))
                .append(" | activo=").append(c.isActivo() ? "si" : "no")
                .append(" | destino=").append(opt(c.getDestino()))
                .append(" | montoAlquiler=").append(opt(c.getMontoAlquiler()))
                .append(" | duracionMeses=").append(opt(c.getDuracion()))
                .append(" | actualizacionMeses=").append(opt(c.getActualizacion()))
                .append(" | inicio=").append(opt(c.getFecha_inicio()))
                .append(" | fin=").append(opt(c.getFecha_fin()))
                .append('\n');

        // ===== PROPIEDAD =====
        if (c.getPropiedad() != null) {
            var p = c.getPropiedad();
            StringBuilder line = new StringBuilder("PROPIEDAD|");
            kv(line, "id", p.getId_propiedad());
            kv(line, "tipo", p.getTipo());
            kv(line, "direccion", p.getDireccion());
            kv(line, "localidad", p.getLocalidad());
            kv(line, "partido", p.getPartido());
            kv(line, "provincia", p.getProvincia());
            kv(line, "inventario", p.getInventario());
            line.append(" | disponible=").append(p.isDisponibilidad() ? "si" : "no");
            sb.append(line).append('\n');
        }

        // ===== INQUILINO =====
        if (c.getInquilino() != null) {
            var i = c.getInquilino();
            StringBuilder line = new StringBuilder("INQUILINO|");
            kv(line, "nombre", i.getNombre());
            kv(line, "apellido", i.getApellido());
            kv(line, "pronombre", i.getPronombre());
            kv(line, "nacionalidad", i.getNacionalidad());
            kv(line, "estado_civil", i.getEstadoCivil());
            kv(line, "direccion_residencial", i.getDireccionResidencial());
            kv(line, "telefono", i.getTelefono());
            kv(line, "email", i.getEmail());
            sb.append(line).append('\n');
        }

        // ===== PROPIETARIO =====
        if (c.getPropietario() != null) {
            var pr = c.getPropietario();
            StringBuilder line = new StringBuilder("PROPIETARIO|");
            kv(line, "nombre", pr.getNombre());
            kv(line, "apellido", pr.getApellido());
            kv(line, "pronombre", pr.getPronombre());
            kv(line, "nacionalidad", pr.getNacionalidad());
            kv(line, "estado_civil", pr.getEstadoCivil());
            kv(line, "direccion_residencial", pr.getDireccionResidencial());
            kv(line, "telefono", pr.getTelefono());
            kv(line, "email", pr.getEmail());
            sb.append(line).append('\n');
        }

        // ===== SERVICIOS =====
        sb.append("SERVICIOS|")
                .append("agua=").append(opt(c.getAguaEmpresa())).append(" ").append(opt(c.getAguaPorcentaje())).append("%")
                .append(" | gas=").append(opt(c.getGasEmpresa())).append(" ").append(opt(c.getGasPorcentaje())).append("%")
                .append(" | luz=").append(opt(c.getLuzEmpresa())).append(" ").append(opt(c.getLuzPorcentaje())).append("%")
                .append(" | municipal=").append(opt(c.getMunicipalEmpresa())).append(" ").append(opt(c.getMunicipalPorcentaje())).append("%")
                .append('\n');

        // ===== GARANTES =====
        if (c.getGarantes() != null && !c.getGarantes().isEmpty()) {
            for (var g : c.getGarantes()) {
                StringBuilder line = new StringBuilder("GARANTE|");
                kv(line, "id", g.getId());
                kv(line, "nombre", g.getNombre());
                kv(line, "apellido", g.getApellido());
                kv(line, "pronombre", g.getPronombre());
                kv(line, "nacionalidad", g.getNacionalidad());
                kv(line, "telefono", g.getTelefono());
                kv(line, "email", g.getEmail());
                kv(line, "direccion", g.getDireccion());
                kv(line, "cargo_actual", g.getCargoActual());
                kv(line, "sector_actual", g.getSectorActual());
                kv(line, "nombre_empresa", g.getNombreEmpresa());
                kv(line, "cuit_empresa", g.getCuitEmpresa());
                kv(line, "legajo", g.getLegajo());
                kv(line, "tipo_garantia", g.getTipoGarantia());
                kv(line, "tipo_propiedad", g.getTipoPropiedad());
                kv(line, "partida_inmobiliaria", g.getPartidaInmobiliaria());
                kv(line, "info_catastral", g.getInfoCatastral());
                kv(line, "estado_ocupacion", g.getEstadoOcupacion());
                kv(line, "informe_dominio", g.getInformeDominio());
                kv(line, "informe_inhibicion", g.getInformeInhibicion());
                sb.append(line).append('\n');
            }
        }

        // ===== NOTAS =====
        if (c.getNotas() != null && !c.getNotas().isEmpty()) {
            for (Nota n : c.getNotas()) {
                StringBuilder line = new StringBuilder("NOTA|");
                kv(line, "id", n.getId());
                kv(line, "tipo", n.getTipo());
                kv(line, "fecha", n.getFechaCreacion());
                kv(line, "estado", n.getEstado());
                kv(line, "prioridad", n.getPrioridad());
                kv(line, "motivo", n.getMotivo());
                kv(line, "contenido", n.getContenido());
                kv(line, "observaciones", n.getObservaciones());
                sb.append(line).append('\n');
            }
        }

        return cap(sb.toString());
    }






    private void insertContratoEnSupabase(Long idContrato, String titulo, String contenido,
                                          Long userId, String username, LocalDate fechaInicio,
                                          LocalDate fechaFin) throws IOException {

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> registro = Map.of(
                "id", idContrato,
                "titulo", titulo,
                "contenido", contenido,
                "user_id", userId,
                "username", username,
                "fecha_inicio", fechaInicio != null ? fechaInicio.toString() : null,
                "fecha_fin", fechaFin != null ? fechaFin.toString() : null
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/contratos")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase Error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Contrato guardado en Supabase: {}", idContrato);
        }
    }



    private void guardarEnSupabase(
            Long idContrato,
            String contenido,
            List<Float> embedding,
            Long userId,
            Long propietarioId,
            Long inquilinoId,
            Long propiedadId,
            List<Long> reciboIds,
            List<Long> notaIds,
            List<Long> garantesIds
    ) throws IOException {

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // ✅ vector: se envía como JSON Array (NO string)
        Map<String, Object> registro = Map.of(
                "id_contrato", idContrato,
                "contenido", contenido,
                "embedding", embedding,
                "user_id", userId,
                "id_propietario", propietarioId,
                "id_inquilino", inquilinoId,
                "id_propiedad", propiedadId,
                "ids_recibos", reciboIds,
                "ids_notas", notaIds,
                "ids_garantes", garantesIds
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/contratos_embeddings")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Accept-Profile", "public") // ✅ clave correcta para REST
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase Error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Embedding insertado correctamente: contrato {}", idContrato);
        }
    }


    private LocalDate resolverFechaFin(Contrato contrato) {
        if (contrato.getFecha_fin() != null) {
            return contrato.getFecha_fin();
        }
        if (contrato.getFecha_inicio() != null && contrato.getDuracion() > 0) {
            return contrato.getFecha_inicio().plusMonths(contrato.getDuracion());
        }
        return null;
    }
    private LocalDate resolverFechaFin(LocalDate fechaFin, LocalDate fechaInicio, Integer duracion) {
        if (fechaFin != null) {
            return fechaFin;
        }
        if (fechaInicio != null && duracion != null && duracion > 0) {
            return fechaInicio.plusMonths(duracion);
        }
        return null;
    }
    private List<Garante> clonarGarantes(Contrato contratoBase, Contrato contratoNuevo) {
        List<Garante> originales = Optional.ofNullable(contratoBase.getGarantes())
                .orElseGet(Collections::emptyList);
        List<Garante> clones = new ArrayList<>();

        for (Garante original : originales) {
            Garante clone = new Garante();
            clone.setPronombre(original.getPronombre());
            clone.setNombre(original.getNombre());
            clone.setApellido(original.getApellido());
            clone.setTelefono(original.getTelefono());
            clone.setEmail(original.getEmail());
            clone.setDni(original.getDni());
            clone.setCuit(original.getCuit());
            clone.setDireccionResidencial(original.getDireccionResidencial());
            clone.setNacionalidad(original.getNacionalidad());
            clone.setEstadoCivil(original.getEstadoCivil());
            clone.setTipoGarantia(original.getTipoGarantia());
            clone.setNombreEmpresa(original.getNombreEmpresa());
            clone.setSectorActual(original.getSectorActual());
            clone.setCargoActual(original.getCargoActual());
            clone.setLegajo(original.getLegajo());
            clone.setCuitEmpresa(original.getCuitEmpresa());
            clone.setPartidaInmobiliaria(original.getPartidaInmobiliaria());
            clone.setDireccion(original.getDireccion());
            clone.setInfoCatastral(original.getInfoCatastral());
            clone.setEstadoOcupacion(original.getEstadoOcupacion());
            clone.setTipoPropiedad(original.getTipoPropiedad());
            clone.setInformeDominio(original.getInformeDominio());
            clone.setInformeInhibicion(original.getInformeInhibicion());
            clone.setUsuario(original.getUsuario());
            clone.setContrato(contratoNuevo);
            clones.add(clone);
        }

        return clones;
    }


    /**
     * Convierte una lista de Long en un array PostgreSQL (ej: {1,2,3})
     */
    private String listToPgArray(List<Long> lista) {
        if (lista == null || lista.isEmpty()) return "{}";
        return "{" + lista.stream().map(String::valueOf).collect(Collectors.joining(",")) + "}";
    }




    @Transactional
    public void actualizarContratoEnSupabasePorId(Long contratoId)
            throws IOException, InterruptedException {

        LOGGER.info("🔄 Actualizando contrato {} en Supabase...", contratoId);

        // 1️⃣ Traer contrato con UNA bag
        Contrato contrato = contratoRepository.findConGarantes(contratoId);
        if (contrato == null) {
            LOGGER.warn("Contrato {} no encontrado", contratoId);
            return;
        }

        // 2️⃣ Inicializar la segunda bag en la MISMA sesión
        contrato.getNotas().size();

        // 3️⃣ Construir contenido plano
        String contenido = buildContenidoContratoPlano(contrato);

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // 4️⃣ UPSERT contrato
        Map<String, Object> registroContrato = Map.of(
                "id", contrato.getId(),
                "titulo", contrato.getNombreContrato(),
                "contenido", contenido,
                "fecha_inicio", contrato.getFecha_inicio() != null ? contrato.getFecha_inicio().toString() : null,
                "fecha_fin", contrato.getFecha_fin() != null ? contrato.getFecha_fin().toString() : null,
                "username", contrato.getUsuario().getUsername(),
                "user_id", contrato.getUsuario().getId()
        );

        Request contratoReq = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/contratos")
                .post(RequestBody.create(
                        mapper.writeValueAsString(List.of(registroContrato)),
                        MediaType.parse("application/json")
                ))
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .build();

        try (Response r = client.newCall(contratoReq).execute()) {
            if (!r.isSuccessful()) {
                throw new IOException("Supabase contrato error: " + r.body().string());
            }
        }

        // 5️⃣ Embedding
        List<Float> embedding = embeddingService.generarEmbedding(contenido);

        Map<String, Object> registroEmbedding = Map.of(
                "id_contrato", contrato.getId(),
                "embedding", embedding,
                "contenido", contenido,
                "user_id", contrato.getUsuario().getId()
        );

        Request embReq = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/contratos_embeddings")
                .post(RequestBody.create(
                        mapper.writeValueAsString(List.of(registroEmbedding)),
                        MediaType.parse("application/json")
                ))
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "resolution=merge-duplicates")
                .build();

        try (Response r = client.newCall(embReq).execute()) {
            if (!r.isSuccessful()) {
                throw new IOException("Supabase embedding error: " + r.body().string());
            }
        }

        LOGGER.info("✅ Contrato {} sincronizado correctamente", contratoId);
    }


    @Transactional
    public void generarEmbeddingsParaUsuario(Long userId) {

        List<Long> ids = contratoRepository.findIdsByUsuarioId(userId);

        ids.forEach(id -> {
            try {
                actualizarContratoEnSupabasePorId(id);
            } catch (Exception e) {
                LOGGER.error("❌ Error contrato {}: {}", id, e.getMessage());
            }
        });

        LOGGER.info("✅ Embeddings generados para {} contratos del usuario {}", ids.size(), userId);
    }



    @Transactional
    public void regenerarEmbeddingsTodosLosContratos() {

        List<Long> ids = contratoRepository.findAllIds();

        ids.forEach(id -> {
            try {
                actualizarContratoEnSupabasePorId(id);
            } catch (Exception e) {
                LOGGER.error("❌ Error contrato {}: {}", id, e.getMessage());
            }
        });

        LOGGER.info("🔁 Regeneración global finalizada");
    }

    @Transactional
    private void validarContratoEntrada(ContratoEntradaDto dto) {
//        if (dto.getNombreUsuario() == null || dto.getNombreUsuario().isEmpty()) {
//            throw new IllegalArgumentException("El nombre de usuario no puede ser nulo o vacío");
//        }
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

    private void validarLimiteDeContratos() {
        // 🔹 1. Obtener autenticación actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        // 🔹 2. Obtener userId desde los detalles
        Long usuarioId = null;
        if (auth.getDetails() instanceof Map<?,?> details && details.get("userId") != null) {
            usuarioId = ((Number) details.get("userId")).longValue();
        }

        if (usuarioId == null) {
            throw new RuntimeException("No se encontró userId en el token JWT");
        }

        // 🔹 3. Si es SUPER_ADMIN → sin límite
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin) {
            return; // ✅ no se aplica ningún límite
        }

        // 🔹 4. Buscar la suscripción
        Optional<Subscription> optSub = subscriptionRepository.findByUsuarioId(usuarioId);

        int limite;
        String nombrePlan;

        if (optSub.isPresent() && optSub.get().getPlan() != null) {
            Plan plan = optSub.get().getPlan();
            limite = plan.getContractLimit();
            nombrePlan = plan.getName();

            // Si la suscripción no está activa → usar FREE
            if (optSub.get().getStatus() != Subscription.Status.ACTIVE) {
                limite = 3;
                nombrePlan = "FREE";
            }
        } else {
            // Sin suscripción → plan FREE
            Plan free = planRepository.findByCode("FREE")
                    .orElseThrow(() -> new ResourceNotFoundException("Plan FREE no encontrado"));
            limite = free.getContractLimit();
            nombrePlan = free.getName();
        }

        // 🔹 5. Validar si superó el límite
        long usados = contratoRepository.countActivosByUsuario(usuarioId);
        if (limite >= 0 && usados >= limite) {
            throw new ContractLimitExceededException(String.format(
                    "Alcanzaste tu límite de %d contratos del plan '%s'. " +
                            "Actualizá tu suscripción para crear más.", limite, nombrePlan));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratoSalidaDto> buscarContratoPorUsuario(String username) {
        List<Contrato> contratoList = contratoRepository.findContratosByUsuarioUsername(username);

        return contratoList.stream()
                .map(contrato -> {
                    // Ahora solo inicializamos la colección que NO se hizo con FETCH JOIN
                    if (contrato.getRecibos() != null){
                        Hibernate.initialize(contrato.getRecibos());
                    }
                    for (Recibo recibo : contrato.getRecibos()) {
                        Hibernate.initialize(recibo.getImpuestos()); //
                    }
                    Long tiempoRestante = null;
                    try {
                        tiempoRestante = verificarFinalizacionContrato(contrato.getId());
                    } catch (ResourceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    contrato.setTiempoRestante(tiempoRestante);
                    return modelMapper.map(contrato, ContratoSalidaDto.class);
                })

                .toList();
    }

//    @Override
//    @Transactional
//    public List<ContratoSalidaDto> buscarContratoPorUsuario(String username) {
//
//        List<Contrato> contratoList = contratoRepository.findContratosByUsername(username);
//        return contratoList.stream()
//                .map(contrato -> {
//                    if (contrato.getGarantes() != null) {
//                        contrato.setGarantes(new ArrayList<>(contrato.getGarantes()));
//                    }
//                    if (contrato.getRecibos() != null){
//                        Hibernate.initialize(contrato.getRecibos());
//                    }
//                    for (Recibo recibo : contrato.getRecibos()) {
//                        Hibernate.initialize(recibo.getImpuestos()); //
//                    }
//                    Long tiempoRestante = null;
//                    try {
//                        tiempoRestante = verificarFinalizacionContrato(contrato.getId_contrato());
//                    } catch (ResourceNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//                    contrato.setTiempoRestante(tiempoRestante);
//                    return modelMapper.map(contrato, ContratoSalidaDto.class);
//                })
//                .toList();
//    }

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

        Contrato contrato = contratoRepository.findContratoBase(id);

        if (contrato == null) {
            throw new EntityNotFoundException("Contrato no encontrado");
        }

        // 1) Garantes
        List<Garante> garantes = garanteRepository.findGarantesByContratoId(id);
        contrato.setGarantes(garantes);

        // 2) Notas
        List<Nota> notas = notaRepository.findByContratoId(id);
        contrato.setNotas(notas);

        contrato.setRecibos(new ArrayList<>());

        return modelMapper.map(contrato, ContratoSalidaDto.class);
    }

    @Transactional
    public ContratoPdfDto buscarContratoPdf(Long id) {

        Contrato contrato = contratoRepository.findContratoPdfBase(id);

        if (contrato == null) {
            throw new EntityNotFoundException("Contrato no encontrado");
        }

        // Garantes sin generar bags
        List<Garante> garantes = garanteRepository.findGarantesByContratoId(id);

        // Armado manual del DTO (MUY rápido y seguro)
        ContratoPdfDto dto = new ContratoPdfDto();

        dto.setId(contrato.getId());
        dto.setNombreContrato(contrato.getNombreContrato());
        dto.setFecha_inicio(contrato.getFecha_inicio());
        dto.setFecha_fin(contrato.getFecha_fin());
        dto.setActualizacion(contrato.getActualizacion());
        dto.setMontoAlquiler(contrato.getMontoAlquiler());
        dto.setDuracion(contrato.getDuracion());
        dto.setActivo(contrato.isActivo());

        dto.setAguaEmpresa(contrato.getAguaEmpresa());
        dto.setAguaPorcentaje(contrato.getAguaPorcentaje());
        dto.setLuzEmpresa(contrato.getLuzEmpresa());
        dto.setLuzPorcentaje(contrato.getLuzPorcentaje());
        dto.setGasEmpresa(contrato.getGasEmpresa());
        dto.setGasPorcentaje(contrato.getGasPorcentaje());
        dto.setMunicipalEmpresa(contrato.getMunicipalEmpresa());
        dto.setMunicipalPorcentaje(contrato.getMunicipalPorcentaje());

        dto.setIndiceAjuste(contrato.getIndiceAjuste());
        dto.setMontoAlquilerLetras(contrato.getMontoAlquilerLetras());
        dto.setMultaXDia(contrato.getMultaXDia());
        dto.setTiempoRestante(contrato.getTiempoRestante());
        dto.setDestino(contrato.getDestino());
        dto.setTipoGarantia(contrato.getTipoGarantia());

        dto.setContratoPdf(contrato.getPdfContratoTexto());

        // Relaciones
        dto.setPropietario(modelMapper.map(contrato.getPropietario(), PropietarioContratoDtoSalida.class));
        dto.setInquilino(modelMapper.map(contrato.getInquilino(), InquilinoContratoDtoSalida.class));
        dto.setPropiedad(modelMapper.map(contrato.getPropiedad(), PropiedadContratoSalidaDto.class));
        dto.setUsuarioDtoSalida(modelMapper.map(contrato.getUsuario(), UsuarioDtoSalida.class));

        // garantes
        dto.setGarantes(
                garantes.stream()
                        .map(g -> modelMapper.map(g, GaranteSalidaDto.class))
                        .toList()
        );

        return dto;
    }

    @Transactional
    @Override
    public void eliminarContrato(Long id) throws ResourceNotFoundException {
        Logger logger = LoggerFactory.getLogger(ContratoService.class);
        logger.debug("Iniciando eliminación del contrato con ID: {}", id);

        // Buscar el contrato
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Contrato no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Contrato no encontrado con el id: " + id);
                });

        // Verificar si está activo 🚫
        if (contrato.isActivo()) {
            logger.warn("Intento de eliminar contrato activo: {}", id);
            throw new IllegalStateException("No se puede eliminar un contrato activo");
        }

        try {

            // ✅ Liberar la propiedad
            Propiedad propiedad = contrato.getPropiedad();
            if (propiedad != null && !propiedad.isDisponibilidad()) {
                propiedad.setDisponibilidad(true);
                propiedadRepository.save(propiedad);
                logger.debug("Propiedad {} marcada como disponible", propiedad.getId_propiedad());
            }

            // ✅ Inquilino vuelve a quedar sin contrato activo
            Inquilino inquilino = contrato.getInquilino();
            if (inquilino != null && inquilino.isActivo()) {
                inquilino.setActivo(false);
                inquilinoRepository.save(inquilino);
                logger.debug("Inquilino {} marcado como inactivo", inquilino.getId());
            }

            // ✅ Eliminar ingresos mensuales del contrato
            ingresoMensualRepository.deleteByContratoId(id);
            logger.debug("Ingresos mensuales eliminados para contrato {}", id);

            // ✅ Eliminar dependencias antes del contrato
            logger.debug("Eliminando notas, impuestos y recibos del contrato {}", id);

            notaRepository.deleteByContratoId(id);
            impuestoRepository.deleteByContratoId(id);
            reciboRepository.deleteByContratoId(id);

            // ✅ Eliminar contrato
            contratoRepository.delete(contrato);

            logger.info("Contrato eliminado correctamente con ID: {}", id);

        } catch (DataIntegrityViolationException dive) {
            logger.error("Violación de integridad al eliminar contrato con ID: {}", id, dive);
            throw new RuntimeException("Violación de integridad: " + dive.getMessage());
        } catch (Exception e) {
            logger.error("Error general al eliminar contrato con ID: {}", id, e);
            throw new RuntimeException("Error general al eliminar el contrato: " + e.getMessage());
        }
        CompletableFuture.runAsync(() -> {
            try {
                eliminarIngresosSupabasePorContrato(id);
                eliminarDeSupabase(id);
            } catch (Exception ex) {
                logger.error("⚠️ No se pudo eliminar de Supabase contrato {}: {}", id, ex.getMessage());
            }
        });

    }


    private void eliminarDeSupabase(Long contratoId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 1️⃣ Eliminar embeddings primero
        Request reqEmb = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/contratos_embeddings?id_contrato=eq." + contratoId)
                .delete()
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response resEmb = client.newCall(reqEmb).execute()) {
            if (!resEmb.isSuccessful()) {
                throw new IOException("❌ Error supabase embeddings: " +
                        resEmb.code() + " - " + resEmb.body().string());
            }
            LOGGER.info("🧹 Embeddings eliminados para contrato {}", contratoId);
        }

        // 2️⃣ Eliminar contrato en supabase
        Request reqContra = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/contratos?id=eq." + contratoId)
                .delete()
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response resContra = client.newCall(reqContra).execute()) {
            if (!resContra.isSuccessful()) {
                throw new IOException("❌ Error supabase contratos: " +
                        resContra.code() + " - " + resContra.body().string());
            }
            LOGGER.info("🗑️ Contrato eliminado en Supabase {}", contratoId);
        }
    }

    private void eliminarIngresosSupabasePorContrato(Long contratoId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 🧹 Borrar filas de ingresos_mensuales del contrato
        Request reqIngresos = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/ingresos_mensuales?id_contrato=eq." + contratoId)
                .delete()
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();

        try (Response res = client.newCall(reqIngresos).execute()) {
            if (!res.isSuccessful()) {
                throw new IOException("❌ Error supabase ingresos_mensuales: " +
                        res.code() + " - " + res.body().string());
            }
            LOGGER.info("🧹 Ingresos mensuales eliminados en Supabase para contrato {}", contratoId);
        }
    }


    @Transactional
    @Override
    public Boolean cambiarEstadoContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Contrato no encontrado"));
        contrato.setActivo(!contrato.isActivo());
        contrato.setEstado(contrato.isActivo() ? EstadoContrato.ACTIVO : EstadoContrato.INACTIVO);
        contratoRepository.save(contrato);
        return contrato.isActivo();
    }
    @Transactional
    @Override
    public void finalizarContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        contrato.setActivo(false);
        contrato.setEstado(EstadoContrato.FINALIZADO);
        contratoRepository.save(contrato);

        Propiedad propiedad = contrato.getPropiedad();
        propiedad.setDisponibilidad(true);
        propiedadRepository.save(propiedad);

        Inquilino inquilino = contrato.getInquilino();
        inquilino.setActivo(false);
        inquilinoRepository.save(inquilino);
    }
    @Transactional
    @Override
    public ContratoActualizacionDtoSalida verificarActualizacionContrato(Long id) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
        System.out.println("contrato: " + contrato.getId());

        LocalDate fechaInicio = contrato.getFecha_inicio();
        System.out.println("Fecha inicio: " + fechaInicio);
        if (fechaInicio == null) {
            return new ContratoActualizacionDtoSalida(null, 0, 0, false, " El contrato no tiene fecha de inicio asignada");
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
                    " ¡El contrato ya debería haberse actualizado!"
            );
        }

        Period diferencia = Period.between(ahora, proximaActualizacion);
        System.out.println("Meses restantes: " + diferencia.getMonths());

        return new ContratoActualizacionDtoSalida(
                proximaActualizacion,
                diferencia.getMonths(),
                diferencia.getDays(),
                false,
                " Contrato pendiente de actualización"
        );
    }







    @Transactional
    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void verificarAlertasContratos() {
        List<Contrato> contratos = contratoRepository.findAll();

        for(Contrato contrato : contratos) {
            try{
                verificarActualizacionContrato(contrato.getId());
                verificarFinalizacionContrato(contrato.getId());
            }catch (ResourceNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    @Transactional(readOnly = true)
    @Override
    public List<LatestContratosSalidaDto> getLatestContratos() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AccessDeniedException("No se pudo obtener la autenticación del usuario.");
        }

        // 🔹 Recuperar el userId guardado como detalle en JwtTokenValidator
        Object details = authentication.getDetails();
        Long userId = null;

        if (details instanceof Map<?, ?> mapDetails && mapDetails.get("userId") != null) {
            userId = Long.valueOf(mapDetails.get("userId").toString());
        }

        if (userId == null) {
            throw new AccessDeniedException("No se encontró el ID del usuario en el contexto de seguridad.");
        }

        // 🔹 Buscar los últimos 4 contratos de ese usuario
        List<Contrato> contratos = contratoRepository
                .findLatestContratosByUsuarioId(userId, PageRequest.of(0, 4))
                .getContent();

        LOGGER.info("Se obtuvieron los últimos 4 contratos del usuario con ID: {}", userId);

        return contratos.stream()
                .map(contrato -> {
                    LatestContratosSalidaDto dto = new LatestContratosSalidaDto();
                    dto.setId(contrato.getId());
                    dto.setNombreContrato(contrato.getNombreContrato());
                    dto.setUsuarioDtoSalida(modelMapper.map(contrato.getUsuario(), UsuarioDtoSalida.class));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContratoBasicoDto obtenerContratoBasicoPorInquilino(String username) {

        var inquilino = inquilinoRepository.findBasicoByUsuarioCuenta(username)
                .orElseThrow(() -> new RuntimeException("No se encontró inquilino para este usuario"));

        var contrato = contratoRepository.findByInquilinoId(inquilino.getId())
                .orElseThrow(() -> new RuntimeException("No se encontró contrato para este inquilino"));

        Usuario usuarioInmo = contratoRepository.findUsuarioByContratoId(contrato.getId());


        ContratoBasicoDto dto = new ContratoBasicoDto();
        dto.setId(contrato.getId());
        dto.setNombreContrato(contrato.getNombreContrato());
        dto.setFechaInicio(contrato.getFecha_inicio());
        dto.setFechaFin(contrato.getFecha_fin());
        dto.setDuracion(contrato.getDuracion());
        dto.setDireccionPropiedad(contrato.getPropiedad().getDireccion());
        dto.setContratoPdf(contrato.getPdfContratoTexto());
        dto.setNombreInquilino(inquilino.getNombre());
        dto.setApellidoInquilino(inquilino.getApellido());
        dto.setEstado(contrato.getEstado() != null ? contrato.getEstado().name() : null);

        dto.setUsuarioDtoSalida(new UsuarioDtoSalida());
        dto.getUsuarioDtoSalida().setLogo(usuarioInmo.getLogoInmobiliaria().getImageUrl());
        dto.getUsuarioDtoSalida().setRazonSocial(usuarioInmo.getRazonSocial());
        dto.getUsuarioDtoSalida().setNombreNegocio(usuarioInmo.getNombreNegocio());
        dto.getUsuarioDtoSalida().setMatricula(usuarioInmo.getMatricula());
        dto.getUsuarioDtoSalida().setCuit(usuarioInmo.getCuit());
        dto.getUsuarioDtoSalida().setPartido(usuarioInmo.getPartido());
        dto.getUsuarioDtoSalida().setLocalidad(usuarioInmo.getLocalidad());
        dto.getUsuarioDtoSalida().setProvincia(usuarioInmo.getProvincia());

        dto.setPropietario(new PropietarioContratoDtoSalida());
        dto.getPropietario().setApellido(contrato.getPropietario().getApellido());
        dto.getPropietario().setNombre(contrato.getPropietario().getNombre());
        dto.getPropietario().setDni(contrato.getPropietario().getDni());

        dto.setInquilino(new InquilinoContratoDtoSalida());
        dto.getInquilino().setApellido(contrato.getInquilino().getApellido());
        dto.getInquilino().setDireccionResidencial(contrato.getInquilino().getDireccionResidencial());
        dto.getInquilino().setNombre(contrato.getInquilino().getNombre());
        dto.getInquilino().setDni(contrato.getInquilino().getDni());
        dto.getInquilino().setDireccionResidencial(contrato.getInquilino().getDireccionResidencial());


        return dto;
    }





    private java.math.BigDecimal montoComisionContrato(Contrato c) {
        if (c.getMontoAlquiler() == null || c.getDuracion() <= 0) {
            return java.math.BigDecimal.ZERO;
        }

        // Convertimos Double -> BigDecimal
        java.math.BigDecimal alquiler = java.math.BigDecimal.valueOf(c.getMontoAlquiler());

        java.math.BigDecimal meses = java.math.BigDecimal.valueOf(c.getDuracion());
        java.math.BigDecimal porcentaje = pct(c.getComisionContratoPorc());

        return alquiler.multiply(meses).multiply(porcentaje);
    }


    private java.math.BigDecimal montoComisionMensual(Contrato c) {
        if (c.getMontoAlquiler() == null) return java.math.BigDecimal.ZERO;

        java.math.BigDecimal alquiler = java.math.BigDecimal.valueOf(c.getMontoAlquiler());
        java.math.BigDecimal porcentaje = pct(c.getComisionMensualPorc());

        return alquiler.multiply(porcentaje);
    }

    private java.math.BigDecimal montoMensualPropietario(Contrato c) {
        if (c.getMontoAlquiler() == null) return java.math.BigDecimal.ZERO;

        java.math.BigDecimal alquiler = java.math.BigDecimal.valueOf(c.getMontoAlquiler());
        return alquiler.subtract(montoComisionMensual(c));
    }

    private java.math.BigDecimal pct(java.math.BigDecimal p) {
        return (p == null ? java.math.BigDecimal.ZERO : p)
                .divide(java.math.BigDecimal.valueOf(100));
    }

    @Transactional(readOnly = true)
    public ContratoSalidaDto buscarContratoPorNombre(String nombreContrato) throws ResourceNotFoundException {
        Contrato contrato = contratoRepository.findByNombreContratoCompleto(nombreContrato)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró contrato con el nombre: " + nombreContrato));

        // Inicializamos las colecciones lazy
        Hibernate.initialize(contrato.getGarantes());
        Hibernate.initialize(contrato.getRecibos());
        contrato.getRecibos().forEach(recibo -> Hibernate.initialize(recibo.getImpuestos()));

        // 🔹 Primero hacemos el mapeo
        ContratoSalidaDto contratoDto = modelMapper.map(contrato, ContratoSalidaDto.class);

        // 🔹 Luego seteamos el logo manualmente si existe
        Usuario usuario = contrato.getUsuario();
        if (usuario != null && usuario.getLogoInmobiliaria() != null && contratoDto.getUsuarioDtoSalida() != null) {
            contratoDto.getUsuarioDtoSalida().setLogo(usuario.getLogoInmobiliaria().getImageUrl());
        }

        return contratoDto;
    }
    @Transactional(readOnly = true)
    public List<ContratoSalidaDto> listarContratosPorPropietario(String emailPropietario) {
        List<Contrato> contratos = contratoRepository.findByPropietarioEmail(emailPropietario);

        for (Contrato contrato : contratos) {
            Hibernate.initialize(contrato.getRecibos());
            for (Recibo recibo : contrato.getRecibos()) {
                Hibernate.initialize(recibo.getImpuestos());
            }

            // 🔹 Inicializamos también el usuario y su logo
            Hibernate.initialize(contrato.getUsuario());
            if (contrato.getUsuario() != null) {
                Hibernate.initialize(contrato.getUsuario().getLogoInmobiliaria());
            }
        }

        return contratos.stream()
                .map(c -> {
                    ContratoSalidaDto dto = modelMapper.map(c, ContratoSalidaDto.class);

                    // 🔹 Seteamos el logo manualmente si existe
                    Usuario usuario = c.getUsuario();
                    if (usuario != null && usuario.getLogoInmobiliaria() != null && dto.getUsuarioDtoSalida() != null) {
                        dto.getUsuarioDtoSalida().setLogo(usuario.getLogoInmobiliaria().getImageUrl());
                    }

                    return dto;
                })
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ContratoSalidaDto> listarContratosPorUsuarioId(Long userId) {

        List<Contrato> contratoList = contratoRepository.findByUsuarioId(userId);

        return contratoList.stream()
                .map(contrato -> {
                    if (contrato.getRecibos() != null) {
                        Hibernate.initialize(contrato.getRecibos());
                    }
                    for (Recibo recibo : contrato.getRecibos()) {
                        Hibernate.initialize(recibo.getImpuestos());
                    }


                    Long tiempoRestante = null;
                    try {
                        tiempoRestante = verificarFinalizacionContrato(contrato.getId());
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
    public ContratoVencimientoAlertaDto actualizarEstadoAlerta(ContratoAlertaEstadoDto dto) throws ResourceNotFoundException {
        if (dto.getContratoId() == null) {
            throw new IllegalArgumentException("El contratoId es obligatorio");
        }
        Long usuarioId = dto.getUsuarioId() != null ? dto.getUsuarioId() : obtenerUsuarioIdActual();
        if (usuarioId == null) {
            throw new IllegalArgumentException("El usuarioId es obligatorio");
        }

        ContratoAlerta alerta = contratoAlertaRepository.findByContratoIdAndUsuarioId(dto.getContratoId(), usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));

        if (dto.getVisto() != null) {
            alerta.setVisto(dto.getVisto());
        }
        if (dto.getNoMostrar() != null) {
            alerta.setNoMostrar(dto.getNoMostrar());
        }

        ContratoAlerta guardada = contratoAlertaRepository.save(alerta);
        return construirDtoAlerta(guardada);
    }

    private Long obtenerUsuarioIdActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getDetails() instanceof Map<?, ?> details && details.get("userId") != null) {
            return ((Number) details.get("userId")).longValue();
        }
        return null;
    }

    private ContratoAlerta upsertAlerta(Contrato contrato, LocalDate fechaFin, int diasAviso) {
        Long usuarioId = contrato.getUsuario().getId();
        ContratoAlerta alerta = contratoAlertaRepository
                .findByContratoIdAndUsuarioId(contrato.getId(), usuarioId)
                .orElseGet(() -> {
                    ContratoAlerta nueva = new ContratoAlerta();
                    nueva.setContrato(contrato);
                    nueva.setUsuario(contrato.getUsuario());
                    nueva.setVisto(false);
                    nueva.setNoMostrar(false);
                    return nueva;
                });

        alerta.setFechaFin(fechaFin);
        alerta.setDiasAviso(diasAviso);
        return contratoAlertaRepository.save(alerta);
    }
    private ContratoAlerta upsertAlerta(ContratoRepository.ContratoAlertaRow row, LocalDate fechaFin, int diasAviso) {
        ContratoAlerta alerta = contratoAlertaRepository
                .findByContratoIdAndUsuarioId(row.getContratoId(), row.getUserId())
                .orElseGet(() -> {
                    ContratoAlerta nueva = new ContratoAlerta();
                    nueva.setContrato(contratoRepository.getReferenceById(row.getContratoId()));
                    nueva.setUsuario(usuarioRepository.getReferenceById(row.getUserId()));
                    nueva.setVisto(false);
                    nueva.setNoMostrar(false);
                    return nueva;
                });

        alerta.setFechaFin(fechaFin);
        alerta.setDiasAviso(diasAviso);
        return contratoAlertaRepository.save(alerta);
    }

    private ContratoVencimientoAlertaDto construirDtoAlerta(ContratoAlerta alerta) {
        Contrato contrato = alerta.getContrato();
        LocalDate hoy = LocalDate.now();
        boolean vencido = !alerta.getFechaFin().isAfter(hoy);
        long diasRestantes = vencido ? 0 : ChronoUnit.DAYS.between(hoy, alerta.getFechaFin());
        return construirDtoAlerta(alerta, contrato, diasRestantes, vencido);
    }

    private ContratoVencimientoAlertaDto construirDtoAlerta(
            ContratoAlerta alerta,
            Contrato contrato,
            long diasRestantes,
            boolean vencido
    ) {
        return new ContratoVencimientoAlertaDto(
                alerta.getId(),
                contrato.getId(),
                contrato.getUsuario().getId(),
                contrato.getNombreContrato(),
                contrato.getFecha_inicio(),
                alerta.getFechaFin(),
                diasRestantes,
                vencido,
                contrato.getEstado() != null ? contrato.getEstado().name() : null,
                contrato.isActivo(),
                contrato.isActivo()
        );
    }

    private ContratoVencimientoAlertaDto construirDtoAlerta(
            ContratoAlerta alerta,
            ContratoRepository.ContratoAlertaRow row,
            long diasRestantes,
            boolean vencido
    ) {
        return new ContratoVencimientoAlertaDto(
                alerta.getId(),
                row.getContratoId(),
                row.getUserId(),
                row.getNombreContrato(),
                row.getFechaInicio(),
                alerta.getFechaFin(),
                diasRestantes,
                vencido,
                row.getEstado(),
                Boolean.TRUE.equals(row.getActivo()),
                Boolean.TRUE.equals(row.getActivo())
        );
    }

    @Override
    public List<ContratoVencimientoAlertaDto> obtenerAlertasVencimiento(Long userId, int diasAviso) {
        return obtenerAlertasVencimientoInterno(userId, diasAviso, true);
    }

    @Transactional
    private List<ContratoVencimientoAlertaDto> obtenerAlertasVencimientoInterno(Long userId, int diasAviso, boolean validarAuth) {

        Long resolvedUsuarioId = userId;

        if (resolvedUsuarioId == null && validarAuth) {
            resolvedUsuarioId = obtenerUsuarioIdActual();
        }

        if (validarAuth && resolvedUsuarioId == null) {
            throw new IllegalStateException("No se pudo determinar el usuario autenticado");
        }

        final Long usuarioId = resolvedUsuarioId;


        int dias = diasAviso > 0 ? diasAviso : 30;
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);

        return contratoRepository.findAlertasVencimientoActivos().stream()
                .filter(row -> usuarioId == null || row.getUserId().equals(usuarioId))
                .map(row -> {
                    LocalDate fechaFin = resolverFechaFin(row.getFechaFin(), row.getFechaInicio(), row.getDuracion());




                    if (fechaFin == null) return null;

                    boolean vencido = !fechaFin.isAfter(hoy);
                    if (!vencido && fechaFin.isAfter(limite)) return null;

                    ContratoAlerta alerta = upsertAlerta(row, fechaFin, dias);
                    if (alerta.isVisto() || alerta.isNoMostrar()) return null;

                    long diasRestantes = vencido ? 0 : ChronoUnit.DAYS.between(hoy, fechaFin);

                    ContratoVencimientoAlertaDto dto = construirDtoAlerta(alerta, row, diasRestantes, vencido);
                    if (dto.getId() == null || dto.getContratoId() == null || dto.getUserId() == null) {
                        LOGGER.warn("Alerta inválida para contrato {} (usuario {}).", row.getContratoId(), row.getUserId());
                        return null;
                    }
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    @Scheduled(cron = "0 * * * * ?")
    public void notificarAlertasVencimiento() {
        List<ContratoVencimientoAlertaDto> alertas = obtenerAlertasVencimientoInterno(null, 30, false);
        LocalDate hoy = LocalDate.now();

        for (ContratoVencimientoAlertaDto alertaDto : alertas) {
            if (alertaDto.getUserId() == null) {
                continue;
            }
            ContratoAlerta alerta = contratoAlertaRepository.findById(alertaDto.getId()).orElse(null);
            if (alerta == null || alerta.isVisto() || alerta.isNoMostrar()) {
                continue;
            }
            if (alerta.getUltimaNotificacion() != null && alerta.getUltimaNotificacion().isEqual(hoy)) {
                continue;
            }

            List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(alertaDto.getUserId());
            if (subs.isEmpty()) {
                continue;
            }

            String titulo = "📅 Contrato próximo a vencer";
            String cuerpo = String.format(
                    "El contrato '%s' vence el %s. Podés renovarlo o finalizarlo.",
                    alertaDto.getNombreContrato(),
                    alertaDto.getFechaFin()
            );

            for (PushSubscription sub : subs) {
                pushNotificationService.enviarNotificacion(sub, titulo, cuerpo);
            }
            alerta.setUltimaNotificacion(hoy);
            contratoAlertaRepository.save(alerta);
        }
    }
    @Transactional
    @Override
    public ContratoSalidaDto renovarContrato(ContratoRenovacionDtoEntrada dto) throws ResourceNotFoundException {
        if (dto.getIdContrato() == null) {
            throw new IllegalArgumentException("El id del contrato es obligatorio");
        }
        if (dto.getNuevaFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }

        Contrato contratoBase = contratoRepository.findById(dto.getIdContrato())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        if (!contratoBase.isActivo()) {
            throw new IllegalStateException("Solo se pueden renovar contratos activos");
        }
        Double nuevoMontoAlquiler = dto.getMontoAlquiler();
        String nuevoMontoAlquilerLetras = dto.getMontoAlquilerLetras();
        Integer nuevaActualizacionDuracion = dto.getActualizacion();
        LocalDate nuevaFechaInicio = dto.getNuevaFechaInicio();
        LocalDate nuevaFechaFin = dto.getNuevaFechaFin();
        String nuevoTipoGarantia = dto.getTipoGarantia();
        int duracionMeses = dto.getDuracionMeses() != null ? dto.getDuracionMeses() : contratoBase.getDuracion();

        if (nuevaFechaFin == null) {
            if (duracionMeses <= 0) {
                throw new IllegalArgumentException("Debe indicar una duración válida si no se especifica fecha de fin");
            }
            nuevaFechaFin = nuevaFechaInicio.plusMonths(duracionMeses);
        }

        if (!nuevaFechaFin.isAfter(nuevaFechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        if (dto.getDuracionMeses() == null) {
            duracionMeses = Math.max(1, (int) ChronoUnit.MONTHS.between(nuevaFechaInicio, nuevaFechaFin));
        }

        Contrato contratoNuevo = new Contrato();
        contratoNuevo.setNombreContrato(contratoBase.getNombreContrato());
        contratoNuevo.setUsuario(contratoBase.getUsuario());
        contratoNuevo.setPropietario(contratoBase.getPropietario());
        contratoNuevo.setInquilino(contratoBase.getInquilino());
        contratoNuevo.setPropiedad(contratoBase.getPropiedad());
        contratoNuevo.setFecha_inicio(nuevaFechaInicio);
        contratoNuevo.setFecha_fin(nuevaFechaFin);
        contratoNuevo.setDuracion(duracionMeses);
        contratoNuevo.setTipoGarantia(nuevoTipoGarantia);
        contratoNuevo.setActualizacion(nuevaActualizacionDuracion);
        contratoNuevo.setMontoAlquiler(nuevoMontoAlquiler);
        contratoNuevo.setMontoAlquilerLetras(nuevoMontoAlquilerLetras);
        contratoNuevo.setMultaXDia(contratoBase.getMultaXDia());
        contratoNuevo.setDestino(contratoBase.getDestino());
        contratoNuevo.setIndiceAjuste(contratoBase.getIndiceAjuste());
        contratoNuevo.setComisionContratoPorc(contratoBase.getComisionContratoPorc());
        contratoNuevo.setComisionMensualPorc(contratoBase.getComisionMensualPorc());
        contratoNuevo.setAguaEmpresa(contratoBase.getAguaEmpresa());
        contratoNuevo.setAguaPorcentaje(contratoBase.getAguaPorcentaje());
        contratoNuevo.setLuzEmpresa(contratoBase.getLuzEmpresa());
        contratoNuevo.setLuzPorcentaje(contratoBase.getLuzPorcentaje());
        contratoNuevo.setGasEmpresa(contratoBase.getGasEmpresa());
        contratoNuevo.setGasPorcentaje(contratoBase.getGasPorcentaje());
        contratoNuevo.setMunicipalEmpresa(contratoBase.getMunicipalEmpresa());
        contratoNuevo.setMunicipalPorcentaje(contratoBase.getMunicipalPorcentaje());
        contratoNuevo.setActivo(true);
        contratoNuevo.setSuscrito(contratoBase.isSuscrito());

        contratoNuevo.setEstado(EstadoContrato.ACTIVO);
        List<EstadoContrato> estados = new ArrayList<>();

        List<Garante> garantes = new ArrayList<>();
        boolean usarGarantesExistentes = false;
        if (dto.getGarantesIds() != null && !dto.getGarantesIds().isEmpty()) {
            garantes = obtenerGarantesPorIds(dto.getGarantesIds());
            usarGarantesExistentes = true;
        } else if (dto.isMantenerGarantes()) {
            garantes = clonarGarantes(contratoBase, contratoNuevo);
        }

        if (!garantes.isEmpty()) {
            for (Garante garante : garantes) {
                garante.setContrato(contratoNuevo);
            }
            contratoNuevo.setGarantes(garantes);
        } else {
            contratoNuevo.setGarantes(Collections.emptyList());
        }

        Contrato contratoPersistido = contratoRepository.save(contratoNuevo);
        if (usarGarantesExistentes && !garantes.isEmpty()) {
            garanteRepository.saveAll(garantes);
        }

        contratoBase.setActivo(false);
        contratoBase.setEstado(EstadoContrato.RENOVADO);
        contratoRepository.save(contratoBase);

        ingresoMensualService.generarParaContrato(contratoPersistido);
        return modelMapper.map(contratoPersistido, ContratoSalidaDto.class);
    }


    public List<ContratoCardDto> listarCardscontratos(Long userId) {
        List<Contrato> contratos = contratoRepository.listarContratoCards(userId);

        return contratos.stream()
                .map(c -> new ContratoCardDto(
                        c.getId(),
                        c.getNombreContrato(),
                        c.getMontoAlquiler(),
                        c.getInquilino().getNombre() + " " + c.getInquilino().getApellido(),
                        c.getPropietario().getNombre() + " " + c.getPropietario().getApellido(),
                        c.getPropiedad().getDireccion(),
                        c.getEstados()
                ))
                .toList();
    }


    @Transactional
    public void regenerarEmbeddingsContrato(Long contratoId) {

        try {
            actualizarContratoEnSupabasePorId(contratoId);
            LOGGER.info("🔁 Embeddings regenerados contrato {}", contratoId);
        } catch (Exception e) {
            LOGGER.error("❌ Error regenerando contrato {}: {}", contratoId, e.getMessage());
        }
    }

    public List<ContractEventDto> getEventosContratos(Long usuarioId, LocalDate from, LocalDate to) {

        List<ContratoEventoRow> contratos = contratoRepository.findEventosRow(usuarioId, from, to);
        List<ContractEventDto> out = new ArrayList<>();

        for (ContratoEventoRow c : contratos) {
            LocalDate inicio = c.fechaInicio();
            LocalDate fin = c.fechaFin();
            if (inicio == null || fin == null) continue;

            // 🔴 VENCIMIENTO
            if (!fin.isBefore(from) && !fin.isAfter(to)) {
                out.add(new ContractEventDto(
                        "venc-" + c.id(),
                        c.id(),
                        "🔴 Vence: " + tituloContrato(c),
                        fin,
                        true,
                        "VENCE"
                ));
            }

            // 🟠 ACTUALIZACIONES
            int cadaMeses = c.actualizacion();
            if (cadaMeses > 0) {
                LocalDate cursor = primerCursorEnRango(inicio, from, cadaMeses);

                while (!cursor.isAfter(fin) && !cursor.isAfter(to)) {
                    if (!cursor.isBefore(from)) {
                        out.add(new ContractEventDto(
                                "act-" + c.id() + "-" + cursor,
                                c.id(),
                                "🟠 Actualiza: " + tituloContrato(c),
                                cursor,
                                true,
                                "ACTUALIZA"
                        ));
                    }
                    cursor = cursor.plusMonths(cadaMeses);
                }
            }
        }

        out.sort(Comparator.comparing(ContractEventDto::start));
        return out;
    }

    private String tituloContrato(ContratoEventoRow c) {
        if (c.nombreContrato() != null && !c.nombreContrato().isBlank()) return c.nombreContrato();
        return "Contrato #" + c.id();
    }

    private LocalDate primerCursorEnRango(LocalDate inicio, LocalDate from, int cadaMeses) {
        if (!inicio.isBefore(from)) return inicio.plusMonths(cadaMeses);

        long meses = java.time.temporal.ChronoUnit.MONTHS.between(inicio, from);
        long saltos = (meses + (cadaMeses - 1)) / cadaMeses; // ceil
        return inicio.plusMonths(saltos * cadaMeses);
    }

    @Transactional
    public ContratoEstadosDto actualizarEstados(Long idContrato, Set<EstadoContrato> nuevosEstados) {
        Contrato contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        contrato.getEstados().clear();
        contrato.getEstados().addAll(nuevosEstados);

        contratoRepository.save(contrato);

        return new ContratoEstadosDto(contrato.getId(), new HashSet<>(contrato.getEstados()));
    }

    // opcional: helpers para agregar / quitar de a uno
    @Transactional
    public ContratoSalidaDto toggleEstado(Long idContrato, EstadoContrato estado) {
        Contrato contrato = contratoRepository.findById(idContrato)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        if (contrato.getEstados().contains(estado)) {
            contrato.getEstados().remove(estado);
        } else {
            contrato.getEstados().add(estado);
        }

        Contrato guardado = contratoRepository.save(contrato);
        return modelMapper.map(guardado, ContratoSalidaDto.class);
    }
//    @Transactional
//    public ContratoSalidaDto  renovarContrato(Long contratoId, RenovarContratoRequest req, Long usuarioId) {
//
//        // ✅ SIEMPRE renovamos el último vigente
//        Contrato original = obtenerContratoVigente(contratoId, usuarioId);
//
//        // ===== Validaciones fechas (obligatorias) =====
//        if (req.fechaInicio() == null || req.fechaFin() == null) {
//            throw new IllegalArgumentException("fechaInicio y fechaFin son obligatorias para renovar");
//        }
//        if (!req.fechaFin().isAfter(req.fechaInicio())) {
//            throw new IllegalArgumentException("fechaFin debe ser posterior a fechaInicio");
//        }
//
//        // ===== Duración: si viene, validar; si no viene, calcular =====
//        int duracionMesesCalculada = (int) java.time.temporal.ChronoUnit.MONTHS.between(
//                req.fechaInicio(), req.fechaFin()
//        );
//        if (duracionMesesCalculada <= 0) {
//            throw new IllegalArgumentException("No se pudo calcular duración válida con esas fechas");
//        }
//
//        int duracionFinal;
//        if (req.duracion() != null) {
//            if (req.duracion() <= 0) throw new IllegalArgumentException("duracion debe ser mayor a 0");
//
//            duracionFinal = req.duracion();
//        } else {
//            duracionFinal = duracionMesesCalculada;
//        }
//
//        // ===== Crear contrato nuevo =====
//        Contrato nuevo = new Contrato();
//
//        // Copia base
//        nuevo.setUsuario(original.getUsuario());
//        nuevo.setPropietario(original.getPropietario());
//        nuevo.setInquilino(original.getInquilino());
//        nuevo.setPropiedad(original.getPropiedad());
//
//        nuevo.setNombreContrato(original.getNombreContrato());
//        nuevo.setPdfContratoTexto(original.getPdfContratoTexto());
//
//        // Servicios (si viene null => copia)
//        nuevo.setAguaEmpresa(req.aguaEmpresa() != null ? req.aguaEmpresa() : original.getAguaEmpresa());
//        nuevo.setAguaPorcentaje(req.aguaPorcentaje() != null ? req.aguaPorcentaje() : original.getAguaPorcentaje());
//        nuevo.setLuzEmpresa(req.luzEmpresa() != null ? req.luzEmpresa() : original.getLuzEmpresa());
//        nuevo.setLuzPorcentaje(req.luzPorcentaje() != null ? req.luzPorcentaje() : original.getLuzPorcentaje());
//        nuevo.setGasEmpresa(req.gasEmpresa() != null ? req.gasEmpresa() : original.getGasEmpresa());
//        nuevo.setGasPorcentaje(req.gasPorcentaje() != null ? req.gasPorcentaje() : original.getGasPorcentaje());
//        nuevo.setMunicipalEmpresa(req.municipalEmpresa() != null ? req.municipalEmpresa() : original.getMunicipalEmpresa());
//        nuevo.setMunicipalPorcentaje(req.municipalPorcentaje() != null ? req.municipalPorcentaje() : original.getMunicipalPorcentaje());
//
//        // Campos editables
//        nuevo.setMontoAlquiler(req.montoAlquiler() != null ? req.montoAlquiler() : original.getMontoAlquiler());
//        nuevo.setActualizacion(req.actualizacion() != null ? req.actualizacion() : original.getActualizacion());
//        nuevo.setIndiceAjuste(req.indiceAjuste() != null ? req.indiceAjuste() : original.getIndiceAjuste());
//        nuevo.setMontoAlquilerLetras(req.montoAlquilerLetras() != null ? req.montoAlquilerLetras() : original.getMontoAlquilerLetras());
//        nuevo.setMultaXDia(req.multaXDia() != null ? req.multaXDia() : original.getMultaXDia());
//        nuevo.setDestino(req.destino() != null ? req.destino() : original.getDestino());
//        nuevo.setTipoGarantia(req.tipoGarantia() != null ? req.tipoGarantia() : original.getTipoGarantia());
//
//        // Comisiones (si las usás en renovación)
//        if (req.comisionContratoPorc() != null) nuevo.setComisionContratoPorc(req.comisionContratoPorc());
//        else nuevo.setComisionContratoPorc(original.getComisionContratoPorc());
//
//        if (req.comisionMensualPorc() != null) nuevo.setComisionMensualPorc(req.comisionMensualPorc());
//        else nuevo.setComisionMensualPorc(original.getComisionMensualPorc());
//
//        // Fechas y duración
//        nuevo.setFecha_inicio(req.fechaInicio());
//        nuevo.setFecha_fin(req.fechaFin());
//        nuevo.setDuracion(duracionFinal);
//
//        // Flags y links
//        nuevo.setActivo(true);
//        nuevo.setEstado(EstadoContrato.ACTIVO);
//        nuevo.setSuscrito(original.isSuscrito());
//        nuevo.setContratoAnterior(original);
//
//        Contrato nuevoGuardado = contratoRepository.save(nuevo);
//
//        // ===== Garantes =====
//        List<Garante> garantesNuevos = new ArrayList<>();
//
//        if (req.garantesIds() == null) {
//            // clonar garantes del original
//            Contrato originalConGarantes = contratoRepository.findContratoByIdWithGarantes(original.getId());
//            if (originalConGarantes.getGarantes() != null) {
//                for (Garante g : originalConGarantes.getGarantes()) {
//                    Garante clone = garanteService.clonarGarante(g);
//                    clone.setContrato(nuevoGuardado);
//                    clone.setUsuario(original.getUsuario());
//                    garantesNuevos.add(clone);
//                }
//            }
//        } else if (!req.garantesIds().isEmpty()) {
//            // clonar garantes seleccionados
//            List<Garante> base = garanteRepository.findAllById(req.garantesIds());
//            for (Garante g : base) {
//                Garante clone = garanteService.clonarGarante(g);
//                clone.setContrato(nuevoGuardado);
//                clone.setUsuario(original.getUsuario());
//                garantesNuevos.add(clone);
//            }
//        }
//
//        if (!garantesNuevos.isEmpty()) {
//            garanteRepository.saveAll(garantesNuevos);
//            nuevoGuardado.setGarantes(garantesNuevos);
//        } else {
//            nuevoGuardado.setGarantes(new ArrayList<>());
//        }
//
//        // ===== Marcar original como renovado =====
//        original.setActivo(false);
//        original.setEstado(EstadoContrato.RENOVADO);
//        original.setFechaRenovacion(LocalDate.now());
//        original.setContratoRenovado(nuevoGuardado);
//        contratoRepository.save(original);
//
//        return modelMapper.map(nuevoGuardado, ContratoSalidaDto.class);
//    }
//
//
//    private Contrato obtenerContratoVigente(Long contratoId, Long usuarioId) {
//
//        Contrato actual = contratoRepository.findByIdAndUsuarioId(contratoId, usuarioId)
//                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));
//
//        // Avanza por la cadena hasta el último
//        while (actual.getContratoRenovado() != null) {
//            Long siguienteId = actual.getContratoRenovado().getId();
//            actual = contratoRepository.findByIdAndUsuarioId(siguienteId, usuarioId)
//                    .orElseThrow(() ->
//                            new IllegalStateException("Cadena de renovación inconsistente"));
//        }
//
//        if (!actual.isActivo()) {
//            throw new IllegalStateException("El contrato vigente no está activo");
//        }
//
//        return actual;
//    }

}
