package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.entrada.ImpuestoEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.ReciboEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ReciboEstadoActualizadoEvent;
import com.backend.crmInmobiliario.DTO.modificacion.ReciboModificacionDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.entrada.NotificarTransferenciaDto;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.LatestContratosSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.recibo.LatestRecibosSalidaDto;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.entity.impuestos.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.ImpuestoRepository;
import com.backend.crmInmobiliario.repository.InquilinoRepository;
import com.backend.crmInmobiliario.repository.ReciboAlertaRepository;
import com.backend.crmInmobiliario.repository.ReciboRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.notificacionesPush.PushSubscriptionRepository;
import com.backend.crmInmobiliario.repository.projections.ReciboSyncProjection;
import com.backend.crmInmobiliario.service.IReciboService;
import com.backend.crmInmobiliario.service.impl.IA.EmbeddingService;
import com.backend.crmInmobiliario.service.impl.notificacionesPush.PushNotificationService;
import com.backend.crmInmobiliario.service.impl.utilsGeneral.ImpuestoCalculoService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import okhttp3.*;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ReciboService implements IReciboService {
    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.key}")
    private String SUPABASE_ANON_KEY;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;
    private final Logger LOGGER = LoggerFactory.getLogger(ReciboService.class);
    private ModelMapper modelMapper;
    private ReciboRepository reciboRepository;
    private ContratoRepository contratoRepository;
    private InquilinoRepository inquilinoRepository;
    private PushNotificationService pushNotificationService;
    private PushSubscriptionRepository pushSubscriptionRepository;
    private ImagenService imagenService;
    private ContratoService contratoService;
    private EmbeddingService embeddingService;
    private ImpuestoRepository impuestoRepository;
    private UsuarioRepository usuarioRepository;
    private ReciboAlertaRepository reciboAlertaRepository;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    private final ImpuestoCalculoService impuestoCalculoService;
    private AuthUtil authUtil;


    public ReciboService(AuthUtil authUtil, UsuarioRepository usuarioRepository, ImpuestoRepository impuestoRepository, EmbeddingService embeddingService, ContratoService contratoService, ImagenService imagenService, PushNotificationService pushNotificationService, PushSubscriptionRepository pushSubscriptionRepository, ModelMapper modelMapper, ReciboRepository reciboRepository, ContratoRepository contratoRepository, InquilinoRepository inquilinoRepository, ImpuestoCalculoService impuestoCalculoService, ReciboAlertaRepository reciboAlertaRepository) {
        this.modelMapper = modelMapper;
        this.reciboRepository = reciboRepository;
        this.contratoRepository = contratoRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.pushNotificationService = pushNotificationService;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.imagenService = imagenService;
        this.embeddingService = embeddingService;
        this.contratoService = contratoService;
        this.impuestoCalculoService = impuestoCalculoService;
        this.impuestoRepository = impuestoRepository;
        this.usuarioRepository = usuarioRepository;
        this.reciboAlertaRepository = reciboAlertaRepository;
        this.authUtil = authUtil;
        configureMapping();
    }

    private void configureMapping() {

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.LOOSE)
                .setAmbiguityIgnored(true);



//        // Verifica si ya existe el TypeMap para evitar duplicados
//        if (modelMapper.getTypeMap(PersistentBag.class, List.class) == null) {
//            modelMapper.createTypeMap(PersistentBag.class, List.class).setConverter(context -> {
//                PersistentBag source = (PersistentBag) context.getSource();
//                return source == null ? null : new ArrayList<>(source);
//            });
//        }

        modelMapper.typeMap(ReciboEntradaDto.class, Recibo.class)
                .addMapping(ReciboEntradaDto::getIdContrato, Recibo::setContrato)
                .addMapping(ReciboEntradaDto::getEstado, Recibo::setEstado)
                .addMapping(ReciboEntradaDto::getConcepto, Recibo::setConcepto)
                .addMapping(ReciboEntradaDto::getFechaEmision, Recibo::setFechaEmision)
                .addMapping(ReciboEntradaDto::getFechaVencimiento, Recibo::setFechaVencimiento)
                .addMapping(ReciboEntradaDto::getMontoTotal, Recibo::setMontoTotal)
                .addMapping(ReciboEntradaDto::getPeriodo, Recibo::setPeriodo)
                .addMapping(ReciboEntradaDto::getImpuestos, Recibo::setImpuestos)
                .addMapping(ReciboEntradaDto::getNumeroRecibo, Recibo::setNumeroRecibo);



        modelMapper.typeMap(Recibo.class, ReciboSalidaDto.class)
                .addMapping(Recibo::getId, ReciboSalidaDto::setId)
                .addMapping(Recibo::getContrato, ReciboSalidaDto::setContratoId)
                .addMapping(Recibo::getFechaEmision, ReciboSalidaDto::setFechaEmision)
                .addMapping(Recibo::getFechaVencimiento, ReciboSalidaDto::setFechaVencimiento)
                .addMapping(Recibo::getPeriodo, ReciboSalidaDto::setPeriodo)
                .addMapping(Recibo::getConcepto, ReciboSalidaDto::setConcepto)
                .addMapping(Recibo::getTransferStatus, ReciboSalidaDto::setTransferStatus)
                .addMapping(Recibo::getNumeroRecibo, ReciboSalidaDto::setNumeroRecibo);

        modelMapper.typeMap(ReciboModificacionDto.class, ReciboSalidaDto.class)
                .addMapping(ReciboModificacionDto::getEstado, ReciboSalidaDto::setEstado);

    }
    @Override
    @Transactional
    public List<ReciboSalidaDto> listarRecibosPorUsuarioId(Long userId) {
        List<Recibo> recibos = reciboRepository.findByContratoUsuarioId(userId);

        return recibos.stream()
                .map(recibo -> modelMapper.map(recibo, ReciboSalidaDto.class))
                .toList();
    }

    @Override
    @Transactional() // Mejora el rendimiento para operaciones de solo lectura
    public List<ReciboSalidaDto> listarRecibos() {
        LOGGER.info("Iniciando el proceso de listado de recibos");
        List<Recibo> recibos = reciboRepository.findAll();
        if (recibos.isEmpty()) {
            LOGGER.warn("No se encontraron recibos");
            return Collections.emptyList(); // Devuelve una lista vacía en lugar de null
        }
        List<ReciboSalidaDto> recibosSalidaDto = recibos.stream()
                .map(recibo -> modelMapper.map(recibo, ReciboSalidaDto.class))
                .collect(Collectors.toList());
        LOGGER.info("Se encontraron " + recibosSalidaDto.size() + " recibos");
        return recibosSalidaDto;
    }


    public List<ReciboSalidaDto> obtenerPorInquilino(Long userId) {
        var inquilino = inquilinoRepository.findByUsuarioCuentaInquilinoId(userId)
                .orElseThrow(() -> new RuntimeException("No se encontró inquilino para este usuario"));

        List<Recibo> recibos = reciboRepository.findByInquilinoIdConTodo(inquilino.getId());
        List<ReciboSalidaDto> dtos = recibos.stream()
                .map(r -> modelMapper.map(r, ReciboSalidaDto.class))
                .toList();
        return dtos;
    }

    @Override
    @Transactional // Importante para la consistencia de la transacción
    public ReciboSalidaDto crearRecibo(ReciboEntradaDto reciboEntradaDto) throws ResourceNotFoundException, IOException {
        LOGGER.info("Iniciando el proceso de creación de recibo");
        Long userId = authUtil.extractUserId();

        Usuario usuario = usuarioRepository.findUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 1. Buscar el contrato
        Contrato contrato = contratoRepository.findById(reciboEntradaDto.getIdContrato())
                .orElseThrow(() -> {
                    LOGGER.error("Contrato no encontrado con ID: " + reciboEntradaDto.getIdContrato());
                    return new ResourceNotFoundException("Contrato no encontrado");
                });

        contratoService.validarAccesoPorSuscripcion(contrato.getUsuario());

        // 2. Crear el recibo
        Recibo recibo = new Recibo();
        recibo.setUsuario(usuario);
        recibo.setContrato(contrato);
        recibo.setMontoTotal(reciboEntradaDto.getMontoTotal());
        recibo.setConcepto(reciboEntradaDto.getConcepto());
        recibo.setPeriodo(reciboEntradaDto.getPeriodo());
        recibo.setFechaEmision(reciboEntradaDto.getFechaEmision());
        recibo.setFechaVencimiento(reciboEntradaDto.getFechaVencimiento());
        recibo.setNumeroRecibo(reciboEntradaDto.getNumeroRecibo());
        // Otros campos del recibo
        // 3. Procesar y agregar los impuestos
        if (reciboEntradaDto.getImpuestos() != null && !reciboEntradaDto.getImpuestos().isEmpty()) {
            for (ImpuestoEntradaDto impuestoDTO : reciboEntradaDto.getImpuestos()) {
                Impuesto impuesto = convertToImpuesto(impuestoDTO); // Usar el método convertToImpuesto
                impuesto.setRecibo(recibo); // Establecer la relación inversa
                recibo.getImpuestos().add(impuesto);
                LOGGER.info("Impuesto agregado al recibo: " + impuesto.getTipoImpuesto());
            }
        } else {
            LOGGER.warn("No se proporcionaron impuestos para el recibo");
            // Considerar si lanzar una excepción aquí es apropiado para tu lógica de negocio
        }
        // 4. Guardar el recibo (y los impuestos en cascada)
        Recibo reciboGuardado = reciboRepository.save(recibo);
        contrato = recibo.getContrato();
        try {
            contratoService.actualizarContratoEnSupabasePorId(contrato.getId());
        } catch (Exception e) {
            LOGGER.error("⚠️ Error al actualizar contrato en Supabase: {}", e.getMessage());
        }
        try {
            Inquilino inquilino = contrato.getInquilino();
            if (inquilino != null && inquilino.getUsuarioCuentaInquilino() != null) {
                Usuario usuarioInquilino = inquilino.getUsuarioCuentaInquilino();
                List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(usuarioInquilino.getId());

                for (PushSubscription sub : subs) {
                    pushNotificationService.enviarNotificacion(
                            sub,
                            "🏠 Nuevo recibo disponible",
                            String.format("Tu recibo del contrato '%s' ya está disponible para descargar.",
                                    contrato.getNombreContrato())
                    );
                }

                LOGGER.info("✅ Notificación enviada al usuario inquilino ID: {}", usuarioInquilino.getId());
            } else {
                LOGGER.warn("⚠️ No se encontró el usuarioCuentaInquilino para el contrato ID: {}", contrato.getId());
            }
        } catch (Exception e) {
            LOGGER.error("❌ Error al enviar notificación push: {}", e.getMessage(), e);
        }

        CompletableFuture.runAsync(() -> {
            try {
                guardarReciboEnSupabase(reciboGuardado);
                upsertReciboEmbedding(reciboGuardado);
            } catch (Exception ex) {
                LOGGER.error("⚠️ Error sincronizando recibo con Supabase: {}", ex.getMessage());
            }
        });

        // 5. Mapear a DTO de salida
        ReciboSalidaDto reciboSalidaDto = modelMapper.map(reciboGuardado, ReciboSalidaDto.class);
        LOGGER.info("Proceso de creación de recibo completado exitosamente");
        return reciboSalidaDto;


    }

    // Método para convertir ImpuestoEntradaDto a Impuesto (como se definió anteriormente)
    private Impuesto convertToImpuesto(ImpuestoEntradaDto dto) {
        // Primero, verifica si el tipo de impuesto es nulo
        if (dto.getTipoImpuesto() == null || dto.getTipoImpuesto().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de impuesto no puede ser nulo o vacío");
        }

        LOGGER.info("---------------------------------------------------------------------------------------");
        LOGGER.info("TIPO IMPUESTO: " + dto.getTipoImpuesto());
        LOGGER.info("---------------------------------------------------------------------------------------");

        // Convierte el tipo de impuesto a mayúsculas para la comparación
        String tipoImpuestoMayus = dto.getTipoImpuesto().toUpperCase();
        Impuesto impuesto;

        // Utiliza un switch para crear la instancia del tipo de impuesto correcto
        switch (tipoImpuestoMayus) {
            case "AGUA":
                impuesto = new Agua();
                break;
            case "GAS":
                impuesto = new Gas();
                break;
            case "LUZ":
                impuesto = new Luz();
                break;
            case "MUNICIPAL":
                impuesto = new Municipal();
                break;
            case "EXP_ORD":
                impuesto = new ExpensaOrdinaria();
                break;
            case "EXP_EXT_ORD":
                impuesto = new ExpensaExtraOrdinaria();
                break;
            case "DEUDA_PENDIENTE":
                impuesto = new DeudaPendiente();
                break;
            case "OTRO":
                impuesto = new Otro();
                break;
            default:
                throw new IllegalArgumentException("Tipo de impuesto no soportado: " + dto.getTipoImpuesto());
        }

        // Mapea los campos comunes del DTO al objeto Impuesto
        impuesto.setTipoImpuesto(dto.getTipoImpuesto()); // Asigna el valor original, no la versión en mayúsculas
        impuesto.setDescripcion(dto.getDescripcion());
        impuesto.setEmpresa(dto.getEmpresa());
        impuesto.setNumeroCliente(dto.getNumeroCliente());
        impuesto.setNumeroMedidor(dto.getNumeroMedidor());
        impuesto.setFechaFactura(dto.getFechaFactura());
        impuesto.setEstadoPago(dto.getEstadoPago());

        BigDecimal montoBase = dto.getMontoBase();
        BigDecimal porcentaje = dto.getPorcentaje();
        if (montoBase == null) {
            throw new IllegalArgumentException("El monto base es obligatorio");
        }

        impuesto.setMontoBase(montoBase);
        impuesto.setPorcentaje(porcentaje);

        BigDecimal montoFinal;
        if (porcentaje != null) {
            montoFinal = impuestoCalculoService
                    .calcularMontoPorcentaje(montoBase, porcentaje);
        } else {
            // 👉 impuesto de monto fijo
            montoFinal = montoBase;
        }

        impuesto.setMontoAPagar(montoFinal);

        // 📎 Archivo factura
        if (dto.getArchivoFactura() != null && !dto.getArchivoFactura().isEmpty()) {
            try {
                String urlFactura = imagenService.subirPdfAFactura(dto.getArchivoFactura());
                impuesto.setUrlFactura(urlFactura);
            } catch (IOException e) {
                LOGGER.error("Error al subir la factura PDF", e);
            }
        }

        return impuesto;
    }
    public void guardarReciboEnSupabase(Recibo r) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        Usuario usuario = r.getContrato().getUsuario();

        // 🔹 Convertir LocalDate → String (ISO) para evitar el error
        String fechaEmision = r.getFechaEmision() != null
                ? r.getFechaEmision().toString()
                : null;

        String fechaVencimiento = r.getFechaVencimiento() != null
                ? r.getFechaVencimiento().toString()
                : null;

        Map<String, Object> registro = Map.of(
                "id", r.getId(),
                "id_contrato", r.getContrato().getId(),
                "user_id", usuario.getId(),
                "numero_recibo", r.getNumeroRecibo(),
                "monto_total", r.getMontoTotal(),
                "periodo", r.getPeriodo(),
                "concepto", r.getConcepto(),
                "fecha_emision", fechaEmision,          // 👈 ahora String
                "fecha_vencimiento", fechaVencimiento,  // 👈 ahora String
                "pagado", r.getEstado()
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/recibos")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Content-Type", "application/json")
                // 👇 upsert: si ya existe el id, hace merge
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase /recibos: " + response.code() + " - " + body);
            }
            LOGGER.info("✅ Recibo upsert en Supabase: {} (pagado={})", r.getId(), r.getEstado());
        }
    }

    @Transactional // Asegura que la transacción esté abierta
    public ReciboSalidaDto buscarReciboPorId(Long id) throws ResourceNotFoundException {
        Optional<Recibo> reciboOptional = reciboRepository.findReciboByIdWithImpuestos(id);

        if (reciboOptional.isPresent()) {
            Recibo recibo = reciboOptional.get();

            return modelMapper.map(recibo, ReciboSalidaDto.class);
        } else {
            throw new ResourceNotFoundException("Recibo no encontrado con ID: " + id);
        }
}
    private String buildContenidoRecibo(Recibo r) {
        return "RECIBO|id=" + r.getId() +
                " | contrato=" + r.getContrato().getNombreContrato() +
                " | periodo=" + r.getPeriodo() +
                " | monto=" + r.getMontoTotal() +
                " | pagado=" + (r.getEstado() ? "sí" : "no");
    }
    public void upsertReciboEmbedding(Recibo r) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        List<Float> embedding = embeddingService.generarEmbedding(buildContenidoRecibo(r));
        Usuario usuario = r.getContrato().getUsuario();

        Map<String, Object> registro = Map.of(
                "id_recibo", r.getId(),
                "id_contrato", r.getContrato().getId(),
                "user_id", usuario.getId(),
                "contenido", buildContenidoRecibo(r),
                "embedding", embedding
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/recibos_embeddings")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase Error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Embedding recibo creado: {}", r.getId());
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void generarEmbeddingsParaUsuario(Long userId) {
        List<Recibo> recibos = reciboRepository.findByUsuarioId(userId);

        recibos.forEach(c -> {
            try {
                upsertReciboEmbedding(c);
            } catch (Exception e) {
                LOGGER.error("Error generando embedding para recibos {}: {}", c.getId(), e.getMessage());
            }
        });

        LOGGER.info("✅ Embeddings generados para {} contratos del usuario {}", recibos.size(), userId);
    }

    @Override
    @Transactional
    public ReciboSalidaDto modificarEstado(ReciboModificacionDto dto) throws ResourceNotFoundException {

        Long reciboId = dto.getId();
        boolean nuevoEstado = dto.getEstado();

        LOGGER.info("Iniciando la modificación del estado del recibo con ID: {}", reciboId);

        // 1) Traer proyección liviana (estado actual + transferStatus + contratoId)
        ReciboRepository.ReciboPagoProjection p = reciboRepository.findPagoProjection(reciboId)
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado con ID: " + reciboId));


        // 3) Update de estado (sin cargar entidad)
        LocalDateTime paidAt = nuevoEstado ? LocalDateTime.now(ZoneOffset.UTC) : null;

        int updated = reciboRepository.updateEstadoYPaidAt(reciboId, nuevoEstado, paidAt);
        if (updated == 0) {
            throw new ResourceNotFoundException("Recibo no encontrado con ID: " + reciboId);
        }

        // 4) Impuestos igual que antes
        int impuestosUpdated = impuestoRepository.updateEstadoPagoByRecibo(reciboId, nuevoEstado);
        LOGGER.info("✅ Se actualizaron {} impuestos del recibo {}", impuestosUpdated, reciboId);

        // 5) Si se desmarca como pago, reseteo transferencia (recomendado)
        if (!nuevoEstado) {
            reciboRepository.resetTransferencia(reciboId, TransferStatus.NONE);
        }

        // 6) Evento AFTER_COMMIT (igual que ya tenías)
        applicationEventPublisher.publishEvent(
                new ReciboEstadoActualizadoEvent(reciboId, p.getContratoId(), nuevoEstado)
        );

        // 7) Respuesta liviana
        ReciboRepository.ReciboEstadoProjection out = reciboRepository.findEstadoProjectionById(reciboId)
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado con ID: " + reciboId));

        ReciboSalidaDto salida = new ReciboSalidaDto();
        salida.setId(out.getId());
        salida.setEstado(out.getEstado());
        salida.setContratoId(out.getContratoId());
        salida.setNombreContrato(out.getNombreContrato());

        return salida;
    }


    @PostConstruct
    void initMapper() {
        modelMapper.typeMap(Recibo.class, ReciboSalidaDto.class)
                .addMappings(m -> {
                    m.map(src -> src.getContrato().getId(), ReciboSalidaDto::setContratoId);
                    m.map(src -> src.getContrato().getNombreContrato(), ReciboSalidaDto::setNombreContrato);
                });
    }

    @Transactional
    public List<ReciboSalidaDto> recibosDelUsuario(Long userId,
                                                   Boolean estado,
                                                   Long contratoId,
                                                   String q) {
        List<Recibo> recibos = reciboRepository.search(userId, estado, contratoId, q);

        return recibos.stream()
                .map(r -> modelMapper.map(r, ReciboSalidaDto.class))
                .toList();
    }

    @Transactional
    public List<ReciboSalidaDto> listarRecibosPorContrato(Long contratoId) throws ResourceNotFoundException {

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado con ID: " + contratoId));

        List<Recibo> recibos = reciboRepository.findByContratoIdConTodo(contratoId);

        // Forzar inicialización de las relaciones
        for (Recibo r : recibos) {
            Hibernate.initialize(r.getContrato().getInquilino());
            Hibernate.initialize(r.getContrato().getPropietario());
            Hibernate.initialize(r.getContrato().getPropiedad());
            Hibernate.initialize(r.getContrato().getUsuario());
        }

        return recibos.stream()
                .map(r -> modelMapper.map(r, ReciboSalidaDto.class))
                .toList();
    }

    @Transactional
    public void eliminarRecibo(Long id) throws ResourceNotFoundException {
        LOGGER.info("Iniciando eliminación del recibo con ID: {}", id);

        Recibo recibo = reciboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado con el ID: " + id));

        Long contratoId = recibo.getContrato() != null
                ? recibo.getContrato().getId()
                : null;

        reciboAlertaRepository.deleteByReciboId(id);
        // 1️⃣ Eliminar en MySQL (transaccional)
        reciboRepository.delete(recibo);
        LOGGER.info("✅ Recibo eliminado correctamente en MySQL con ID: {}", id);

        // 2️⃣ Async SIN entidades JPA
        CompletableFuture.runAsync(() -> {
            try {
                eliminarReciboDeSupabase(id, contratoId);

                if (contratoId != null) {
                    Contrato contrato = contratoRepository
                            .findByIdConFetchCompleto(contratoId)
                            .orElse(null);

                    if (contrato != null) {
                        contratoService.actualizarContratoEnSupabasePorId(contrato.getId());
                        LOGGER.info("🔄 Contrato {} reindexado correctamente", contratoId);
                    }
                }

            } catch (Exception e) {
                LOGGER.error("⚠️ Error procesando eliminación en Supabase", e);
            }
        });
    }
    private void eliminarReciboDeSupabase(Long reciboId, Long contratoId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 1️⃣ Borrar embeddings del recibo
        Request reqEmb = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/recibos_embeddings?id_recibo=eq." + reciboId)
                .delete()
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();
        client.newCall(reqEmb).execute().close();
        LOGGER.info("🧹 Embeddings eliminados para el recibo {}", reciboId);

        // 2️⃣ Borrar recibo en Supabase
        Request reqRecibo = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/recibos?id=eq." + reciboId)
                .delete()
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Prefer", "return=minimal")
                .build();
        client.newCall(reqRecibo).execute().close();
        LOGGER.info("🗑️ Recibo {} eliminado en Supabase", reciboId);

        // 3️⃣ 🔥 BORRAR EMBEDDINGS DEL CONTRATO ENTERO
        if (contratoId != null) {
            Request reqContratoEmb = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/contratos_embeddings?id_contrato=eq." + contratoId)
                    .delete()
                    .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                    .addHeader("Prefer", "return=minimal")
                    .build();

            client.newCall(reqContratoEmb).execute().close();
            LOGGER.info("💣 Embeddings del contrato {} eliminados completamente (chunk viejo eliminado)", contratoId);
        }
    }

    @Transactional
    public void guardarReciboEnSupabasePorProjection(ReciboSyncProjection d)
            throws IOException {

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        // ya vienen como String (o null) desde la proyección
        String fechaEmision = d.getFechaEmision();
        String fechaVencimiento = d.getFechaVencimiento();

        Map<String, Object> registro = Map.of(
                "id", d.getIdRecibo(),
                "id_contrato", d.getContratoId(),
                "user_id", d.getUserId(),
                "numero_recibo", d.getNumeroRecibo(),
                "monto_total", d.getMontoTotal(),
                "periodo", d.getPeriodo(),
                "concepto", d.getConcepto(),
                "fecha_emision", fechaEmision,
                "fecha_vencimiento", fechaVencimiento,
                "pagado", Boolean.TRUE.equals(d.getEstado())
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/recibos")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase /recibos: " + response.code() + " - " + body);
            }
            LOGGER.info("✅ Recibo upsert en Supabase: {} (pagado={})", d.getIdRecibo(), d.getEstado());
        }
    }

    @Transactional
    public void upsertReciboEmbeddingPorProjection(ReciboSyncProjection d)
            throws IOException, InterruptedException {

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String contenido = buildContenidoRecibo(d);
        List<Float> embedding = embeddingService.generarEmbedding(contenido);

        Map<String, Object> registro = Map.of(
                "id_recibo", d.getIdRecibo(),
                "id_contrato", d.getContratoId(),
                "user_id", d.getUserId(),
                "contenido", contenido,
                "embedding", embedding
        );

        String json = mapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/recibos_embeddings")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("❌ Supabase /recibos_embeddings: " + response.code() + " - " + body);
            }
            LOGGER.info("✅ Embedding recibo creado: {}", d.getIdRecibo());
        }
    }

    private String buildContenidoRecibo(ReciboSyncProjection d) {
        return "RECIBO|id=" + d.getIdRecibo() +
                " | contrato=" + d.getNombreContrato() +
                " | periodo=" + d.getPeriodo() +
                " | monto=" + d.getMontoTotal() +
                " | pagado=" + (Boolean.TRUE.equals(d.getEstado()) ? "sí" : "no");
    }

//    @Override
//    @Transactional
//    public void eliminarRecibo(Long id) throws ResourceNotFoundException {
//        LOGGER.info("Iniciando eliminación del recibo con ID: {}", id);
//
//        // 1. Buscar el recibo
//        Recibo recibo = reciboRepository.findById(id)
//                .orElseThrow(() -> {
//                    LOGGER.warn("Recibo no encontrado con ID: {}", id);
//                    return new ResourceNotFoundException("Recibo no encontrado con el ID: " + id);
//                });
//
//        // 2. Eliminar (en cascada se borran también los impuestos)
//        reciboRepository.delete(recibo);
//        LOGGER.info("Recibo eliminado correctamente con ID: {}", id);
//    }
//    @Override
//    @Transactional
//    public void eliminarRecibosPorContratoId(Long contratoId) throws ResourceNotFoundException {
//        LOGGER.info("Iniciando eliminación de todos los recibos del contrato con ID: {}", contratoId);
//
////         1. Verificamos que el contrato exista
//        Contrato contrato = contratoRepository.findById(contratoId)
//                .orElseThrow(() -> {
//                    LOGGER.warn("Contrato no encontrado con ID: {}", contratoId);
//                    return new ResourceNotFoundException("Contrato no encontrado con ID: " + contratoId);
//                });
//
//        // 2. Verificamos si tiene recibos asociados
//        List<Recibo> recibos = reciboRepository.findByContratoId(contratoId);
//        if (recibos.isEmpty()) {
//            LOGGER.warn("No se encontraron recibos para el contrato con ID: {}", contratoId);
//            return;
//        }
//
//        // 3. Eliminamos los recibos (en cascada se eliminan los impuestos también)
//        reciboRepository.deleteAll(recibos);
//        LOGGER.info("Se eliminaron {} recibos del contrato con ID: {}", recibos.size(), contratoId);
//    }


@Transactional
public void notificarTransferencia(Long reciboId, Long userInquilinoId, NotificarTransferenciaDto dto) {

    Usuario usuario = usuarioRepository.findById(userInquilinoId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if (usuario.getInquilino() == null) {
        throw new RuntimeException("Solo un usuario inquilino puede notificar transferencias");
    }

    Recibo recibo = reciboRepository.findById(reciboId)
            .orElseThrow(() -> new RuntimeException("Recibo no encontrado"));

    // Validar pertenencia del recibo al inquilino logueado
    Long inqIdRecibo = recibo.getContrato().getInquilino().getId();
    Long inqIdUsuario = usuario.getInquilino().getId();
    if (!inqIdRecibo.equals(inqIdUsuario)) {
        throw new RuntimeException("Este recibo no pertenece a tu cuenta");
    }

    // Si ya está pago, no tiene sentido
    if (Boolean.TRUE.equals(recibo.getEstado())) {
        throw new RuntimeException("El recibo ya está pago");
    }

    // Ya notificado y pendiente/aprobado => idempotencia / evitar spam
    if (recibo.getTransferStatus() == TransferStatus.PENDING) {
        throw new RuntimeException("Este recibo ya fue notificado y está pendiente de confirmación");
    }
    if (recibo.getTransferStatus() == TransferStatus.APPROVED) {
        throw new RuntimeException("Este recibo ya fue confirmado como pagado");
    }

    // Obtener la inmobiliaria dueña del contrato (usuario “sistema”)
    Usuario inmobiliaria = recibo.getContrato().getUsuario(); // esto depende de tu modelo
    if (inmobiliaria == null) {
        throw new RuntimeException("No se pudo determinar la inmobiliaria del contrato");
    }

    // Validar que haya datos de cobro configurados
    if (inmobiliaria.getMpAlias() == null || inmobiliaria.getMpAlias().isBlank()) {
        throw new RuntimeException("La inmobiliaria no configuró alias de cobro para transferencias");
    }

    // Validación básica de monto (podés permitir parcial si querés)
    BigDecimal esperado = recibo.getMontoTotal() != null ? recibo.getMontoTotal() : BigDecimal.ZERO;
    BigDecimal informado = dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO;




    // Guardar trazabilidad
    recibo.setTransferAlias(inmobiliaria.getMpAlias());
    recibo.setTransferAmount(informado);
    recibo.setTransferNotifiedAt(LocalDateTime.now(ZoneOffset.UTC));
    recibo.setTransferReference(dto.getReference());
    recibo.setTransferComprobanteUrl(dto.getComprobanteUrl());
    recibo.setTransferNote(dto.getNote());
    recibo.setTransferStatus(TransferStatus.PENDING);

    reciboRepository.save(recibo);
    upsertAlertaTransferencia(recibo, inmobiliaria);

    try {
        List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(inmobiliaria.getId());
        if (!subs.isEmpty()) {
            String nombreInquilino = Stream.of(
                            recibo.getContrato().getInquilino().getNombre(),
                            recibo.getContrato().getInquilino().getApellido()
                    )
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(" "));
            String nombreContrato = recibo.getContrato().getNombreContrato() != null
                    ? recibo.getContrato().getNombreContrato()
                    : "Contrato #" + recibo.getContrato().getId();
            String titulo = "💸 Transferencia notificada";
            String cuerpo = String.format(
                    "El inquilino %s notificó el pago del recibo %s por %s.",
                    nombreInquilino.isBlank() ? "del contrato" : nombreInquilino,
                    nombreContrato,
                    informado
            );
            Map<String, Object> data = new HashMap<>();
            data.put("type", "TRANSFERENCIA_PENDIENTE");
            data.put("contratoId", recibo.getContrato().getId());
            data.put("notaId", null);

            for (PushSubscription sub : subs) {
                pushNotificationService.enviarNotificacion(sub, titulo, cuerpo, data);
            }
            LOGGER.info("✅ Notificación push enviada a inmobiliaria ID: {}", inmobiliaria.getId());
        } else {
            LOGGER.info("ℹ️ Sin suscripciones push para inmobiliaria ID: {}", inmobiliaria.getId());
        }
    } catch (Exception e) {
        LOGGER.error("❌ Error al enviar notificación push de transferencia: {}", e.getMessage(), e);
    }
}

private void upsertAlertaTransferencia(Recibo recibo, Usuario inmobiliaria) {
    ReciboAlerta alerta = reciboAlertaRepository
            .findByReciboIdAndUsuarioIdAndTipo(
                    recibo.getId(),
                    inmobiliaria.getId(),
                    TipoAlertaRecibo.TRANSFERENCIA_PENDIENTE
            )
            .orElseGet(() -> {
                ReciboAlerta nueva = new ReciboAlerta();
                nueva.setRecibo(recibo);
                nueva.setUsuario(inmobiliaria);
                nueva.setTipo(TipoAlertaRecibo.TRANSFERENCIA_PENDIENTE);
                return nueva;
            });

    alerta.setVisto(false);
    alerta.setNoMostrar(false);
    alerta.setUltimaNotificacion(LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")));
    reciboAlertaRepository.save(alerta);
}

    @Transactional
    public ReciboSalidaDto aprobarTransferencia(Long reciboId, Long adminUserId) {
        ReciboRepository.ReciboPagoProjection p = reciboRepository.findPagoProjection(reciboId)
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado con ID: " + reciboId));

        if (Boolean.TRUE.equals(p.getEstado())) {
            throw new IllegalStateException("El recibo ya está pagado.");
        }
        if (p.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Solo se puede aprobar una transferencia en estado PENDING.");
        }

        // 1) aprobar + marcar pago (un solo UPDATE)
        int updated = reciboRepository.approveTransferAndMarkPaid(reciboId, TransferStatus.PENDING, TransferStatus.APPROVED);
        if (updated == 0) {
            throw new IllegalStateException("No se pudo aprobar: el estado cambió (race condition).");
        }

        // 2) impuestos pagados
        impuestoRepository.updateEstadoPagoByRecibo(reciboId, true);

        // 3) evento after commit (ya lo usás)
        applicationEventPublisher.publishEvent(new ReciboEstadoActualizadoEvent(reciboId, p.getContratoId(), true));

        // 4) respuesta
        ReciboRepository.ReciboEstadoProjection out = reciboRepository.findEstadoProjectionById(reciboId)
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado con ID: " + reciboId));

        ReciboSalidaDto dto = new ReciboSalidaDto();
        dto.setId(out.getId());
        dto.setEstado(out.getEstado());
        dto.setContratoId(out.getContratoId());
        dto.setNombreContrato(out.getNombreContrato());
        dto.setTransferStatus(TransferStatus.APPROVED);
        return dto;
    }
    @Transactional
    public ReciboSalidaDto rechazarTransferencia(Long reciboId, Long adminUserId) {

        ReciboRepository.ReciboPagoProjection p = reciboRepository.findPagoProjection(reciboId)
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado con ID: " + reciboId));

        // ✅ Validación: el recibo tiene que pertenecer al admin/inmobiliaria
        // (se puede hacer sin cargar toda la entidad con una query auxiliar)
        Long ownerUserId = reciboRepository.findOwnerUserIdByReciboId(reciboId);
        if (ownerUserId == null || !ownerUserId.equals(adminUserId)) {
            throw new IllegalStateException("No tenés permisos para rechazar transferencias de este recibo.");
        }

        if (p.getTransferStatus() != TransferStatus.PENDING) {
            throw new IllegalStateException("Solo se puede rechazar una transferencia en estado PENDING.");
        }

        int updated = reciboRepository.rejectTransfer(reciboId, TransferStatus.PENDING, TransferStatus.REJECTED);
        if (updated == 0) {
            throw new IllegalStateException("No se pudo rechazar: el estado cambió.");
        }

        ReciboRepository.ReciboEstadoProjection out = reciboRepository.findEstadoProjectionById(reciboId)
                .orElseThrow(() -> new ResourceNotFoundException("Recibo no encontrado con ID: " + reciboId));

        ReciboSalidaDto dto = new ReciboSalidaDto();
        dto.setId(out.getId());
        dto.setEstado(out.getEstado());
        dto.setContratoId(out.getContratoId());
        dto.setNombreContrato(out.getNombreContrato());
        dto.setTransferStatus(TransferStatus.REJECTED);
        return dto;
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Override
    public List<LatestRecibosSalidaDto> getLatestRecibos() {

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
        List<Recibo> recibos = reciboRepository
                .findLatestRecibosByUsuarioId(userId, PageRequest.of(0, 4))
                .getContent();

        LOGGER.info("Se obtuvieron los últimos 4 contratos del usuario con ID: {}", userId);

        return recibos.stream()
                .map(recibo -> {
                    LatestRecibosSalidaDto dto = new LatestRecibosSalidaDto();
                    dto.setId(recibo.getId());
                    dto.setNombreContrato(recibo.getContrato().getNombreContrato());
                    dto.setContratoId(recibo.getContrato().getId());
                    dto.setNumeroRecibo(recibo.getNumeroRecibo());
                    dto.setMontoTotal(recibo.getMontoTotal());
                    dto.setFechaEmision(recibo.getFechaEmision());
                    dto.setFechaVencimiento(recibo.getFechaVencimiento());
                    dto.setEstado(recibo.getEstado());
                    dto.setTransferStatus(recibo.getTransferStatus());

                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public List<LatestRecibosSalidaDto> getLatestRecibosPorContrato() {

        Long userId = authUtil.extractUserId(); // mejor que leer SecurityContext a mano

        List<Recibo> recibos = reciboRepository.findLatestReciboPerContratoOwner(userId);

        return recibos.stream().map(recibo -> {
            LatestRecibosSalidaDto dto = new LatestRecibosSalidaDto();
            dto.setId(recibo.getId());
            dto.setNombreContrato(recibo.getContrato().getNombreContrato());
            dto.setContratoId(recibo.getContrato().getId());
            dto.setPeriodo(recibo.getPeriodo());
            dto.setNumeroRecibo(recibo.getNumeroRecibo());
            dto.setMontoTotal(recibo.getMontoTotal());
            dto.setFechaEmision(recibo.getFechaEmision());
            dto.setFechaVencimiento(recibo.getFechaVencimiento());
            dto.setEstado(recibo.getEstado());
            dto.setTransferStatus(recibo.getTransferStatus());
            return dto;
        }).toList();
    }
    private String normalizarPeriodo(String periodo) {
        if (periodo == null) throw new IllegalArgumentException("Periodo obligatorio");
        String p = periodo.trim().toLowerCase(Locale.forLanguageTag("es-AR"));
        // opcional: reemplazar múltiples espacios por uno
        p = p.replaceAll("\\s+", " ");
        return p;
    }

    @Transactional
    public ReciboSalidaDto crearReciboAutomatico(Long contratoId, String periodo) throws IOException {

        Long userId = authUtil.extractUserId();
        Usuario usuario = usuarioRepository.findUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Contrato contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        contratoService.validarAccesoPorSuscripcion(contrato.getUsuario());

        String periodoNorm = normalizarPeriodo(periodo);

        if (reciboRepository.existsByContratoIdAndPeriodo(contratoId, periodoNorm)) {
            throw new IllegalStateException("Ya existe un recibo para ese contrato y período: " + periodoNorm);
        }

        Recibo recibo = new Recibo();
        recibo.setUsuario(usuario);
        recibo.setContrato(contrato);
        recibo.setPeriodo(periodoNorm);

        // fechas: las definís según tu regla (ej: emision hoy, vencimiento día X)
        recibo.setFechaEmision(LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")));
        recibo.setFechaVencimiento(LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")).plusDays(10));

        // concepto / numero
        recibo.setConcepto("Alquiler " + periodoNorm);
        recibo.setNumeroRecibo(generarNumeroRecibo(contrato)); // si ya tenés lógica, usala

        // 1) alquiler base del contrato
        Double alquilerBase = contrato.getMontoAlquiler(); // ajustá al nombre real
        if (alquilerBase == null) alquilerBase = Double.ZERO;

        // 2) impuestos desde plantillas
        List<ContratoImpuestoTemplate> tpls = contratoImpuestoTemplateRepository
                .findByContratoIdAndActivoTrue(contratoId);

        if (tpls != null && !tpls.isEmpty()) {
            for (ContratoImpuestoTemplate t : tpls) {
                ImpuestoEntradaDto dto = new ImpuestoEntradaDto();
                dto.setTipoImpuesto(t.getTipoImpuesto());
                dto.setDescripcion(t.getDescripcion());
                dto.setEmpresa(t.getEmpresa());
                dto.setNumeroCliente(t.getNumeroCliente());
                dto.setNumeroMedidor(t.getNumeroMedidor());

                dto.setMontoBase(t.getMontoBase());      // 👈 base del impuesto
                dto.setPorcentaje(t.getPorcentaje());    // 👈 porcentaje sobre el impuesto

                // dto.setFechaFactura(null); dto.setArchivoFactura(null); etc.

                Impuesto imp = convertToImpuesto(dto);
                imp.setRecibo(recibo);
                recibo.getImpuestos().add(imp);
            }
        }

        // 3) total = alquiler + sum(impuestos)
        BigDecimal totalImpuestos = recibo.getImpuestos().stream()
                .map(Impuesto::getMontoAPagar)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        recibo.setMontoTotal(alquilerBase.add(totalImpuestos));

        Recibo guardado = reciboRepository.save(recibo);

        // sync igual que hoy
        CompletableFuture.runAsync(() -> {
            try {
                guardarReciboEnSupabase(guardado);
                upsertReciboEmbedding(guardado);
                contratoService.actualizarContratoEnSupabasePorId(contrato.getId());
            } catch (Exception e) {
                LOGGER.error("⚠️ Error sync recibo automático: {}", e.getMessage());
            }
        });

        return modelMapper.map(guardado, ReciboSalidaDto.class);
    }
}
