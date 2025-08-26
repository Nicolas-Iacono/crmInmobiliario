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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import okhttp3.*;
import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentBag;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContratoService implements IContratoService {

    private final Logger LOGGER = LoggerFactory.getLogger(ContratoService.class);
    private static final int MAX_EMBEDDING_CHARS = 12_000;

    private static boolean has(Object v) { return v != null && !v.toString().trim().isEmpty(); }
    private static String opt(Object v) { return v == null ? "" : v.toString().trim(); }
    private static void kv(StringBuilder sb, String key, Object val) { if (has(val)) sb.append(" | ").append(key).append("=").append(opt(val)); }
    private static String cap(String s) { return s.length() > MAX_EMBEDDING_CHARS ? s.substring(0, MAX_EMBEDDING_CHARS) : s; }

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
    private NotaRepository notaRepository;
    private ReciboRepository reciboRepository;
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
                           MunicipalRepository municipalRepository,
                           NotaRepository notaRepository,
                           ReciboRepository reciboRepository
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
        this.notaRepository = notaRepository;
        this.reciboRepository = reciboRepository;
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
// Seteos manuales de campos críticos
        contratoEnCreacion.setMontoAlquiler(contratoEntradaDto.getMontoAlquiler());
        contratoEnCreacion.setMultaXDia(contratoEntradaDto.getMultaXDia());
        contratoEnCreacion.setMontoAlquilerLetras(contratoEntradaDto.getMontoAlquilerLetras());
        contratoEnCreacion.setActualizacion(contratoEntradaDto.getActualizacion());
        contratoEnCreacion.setDuracion(contratoEntradaDto.getDuracion());
        contratoEnCreacion.setDestino(contratoEntradaDto.getDestino());
        contratoEnCreacion.setIndiceAjuste(contratoEntradaDto.getIndiceAjuste());
        // Persistir el contrato
        Contrato contratoPersistido = contratoRepository.save(contratoEnCreacion);
        // Intentar embeddings + Supabase (no bloquea la creación)
        try {
            // Aseguramos inicializar colecciones lazy si las vamos a leer
            Hibernate.initialize(contratoPersistido.getGarantes());
            Hibernate.initialize(contratoPersistido.getRecibos());
            if (contratoPersistido.getRecibos() != null) {
                for (Recibo r : contratoPersistido.getRecibos()) Hibernate.initialize(r.getImpuestos());
            }
            Long userId = contratoPersistido.getUsuario().getId();
            Long propietrioId = contratoPersistido.getPropietario().getId();
            Long inquilinoId = contratoPersistido.getInquilino().getId();
            Long propiedadId = contratoPersistido.getPropiedad().getId_propiedad();

            List<Long> garantesIds = Optional.ofNullable(contratoPersistido.getGarantes())
                    .orElseGet(Collections::emptyList)
                    .stream().map(Garante::getId).toList();

            List<Long> reciboIds = Optional.ofNullable(contratoPersistido.getRecibos())
                    .orElseGet(Collections::emptyList)
                    .stream().map(Recibo::getId).toList();

            List<Long> notasIds = Optional.ofNullable(contratoPersistido.getNotas())
                    .orElseGet(Collections::emptyList)
                    .stream().map(Nota::getId).toList();

            String contenido = buildContenidoContrato(contratoPersistido);
            List<Float> embedding = generarEmbedding(contenido); // text-embedding-3-small → 1536 dims
            guardarEnSupabase(   contratoPersistido.getId_contrato(),
                    contenido,
                    embedding,
                    userId,
                    propietrioId,
                    inquilinoId,
                    propiedadId,
                    reciboIds,
                    notasIds,
                    garantesIds);
            LOGGER.info("Embedding guardado en Supabase para contrato {}", contratoPersistido.getId_contrato());
        } catch (Exception e) {
            LOGGER.error("No se pudo guardar embedding en Supabase (contrato {}): {}",
                    contratoPersistido.getId_contrato(), e.getMessage());
        }


        // Cambiar el estado del contrato a activo
        if (!cambiarEstadoContrato(contratoPersistido.getId_contrato())) {
            throw new RuntimeException("No se pudo activar el contrato");
        }

        // Actualizar la disponibilidad de la propiedad
        propiedad.setDisponibilidad(false);
        propiedadRepository.save(propiedad);

        return modelMapper.map(contratoPersistido, ContratoSalidaDto.class);
    }
    private String buildContenidoContrato(Contrato c) {
        StringBuilder sb = new StringBuilder();

        // ===== Resumen contrato =====
        sb.append("CONTRATO|")
                .append("id=").append(c.getId_contrato())
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



    private List<Float> generarEmbedding(String texto) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(Map.of(
                "model", "text-embedding-3-small",
                "input", texto
        ));

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/embeddings")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + System.getenv("OPENAI_APIKEY"))
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error OpenAI: " + response);
            }
            JsonNode root = mapper.readTree(response.body().string());
            JsonNode vector = root.path("data").get(0).path("embedding");

            List<Float> result = new ArrayList<>();
            for (JsonNode num : vector) {
                result.add(num.floatValue());
            }
            return result;
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

        // Convertir listas a formato Postgres ARRAY: {1,2,3}
        String recibosArray = listToPgArray(reciboIds);
        String notasArray = listToPgArray(notaIds);
        String garantesArray = listToPgArray(garantesIds);

        Map<String, Object> registro = Map.of(
                "id_contrato", idContrato,
                "contenido", contenido,
                "embedding", embedding,
                "user_id", userId,             // 👈 corregido
                    "id_propietario", propietarioId,
                "id_inquilino", inquilinoId,
                "id_propiedad", propiedadId,
                "ids_recibos", recibosArray,
                "ids_notas", notasArray,
                "ids_garantes", garantesArray
        );

        String json = mapper.writeValueAsString(List.of(registro)); // Supabase espera array de objetos

        Request request = new Request.Builder()
                .url(System.getenv("SUPABASE_URL") + "/rest/v1/contratos_embeddings")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .addHeader("apikey", System.getenv("SUPABASE_ANON_KEY"))
                .addHeader("Authorization", "Bearer " + System.getenv("SUPABASE_SERVICE_ROLE_KEY"))
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "sin cuerpo";
                throw new IOException("Error Supabase (" + response.code() + "): " + errorBody);
            }
            LOGGER.info("Contrato insertado en Supabase con éxito (id={})", idContrato);
        }
    }

    /**
     * Convierte una lista de Long en un array PostgreSQL (ej: {1,2,3})
     */
    private String listToPgArray(List<Long> lista) {
        if (lista == null || lista.isEmpty()) return "{}";
        return "{" + lista.stream().map(String::valueOf).collect(Collectors.joining(",")) + "}";
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
        Logger logger = LoggerFactory.getLogger(ContratoService.class);
        logger.debug("Iniciando eliminación del contrato con ID: {}", id);

        // Buscar el contrato
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Contrato no encontrado con ID: {}", id);
                    return new ResourceNotFoundException("Contrato no encontrado con el id: " + id);
                });

        // Verificar si está activo
        if (contrato.isActivo()) {
            logger.warn("Intento de eliminar contrato activo: {}", id);
            throw new IllegalStateException("No se puede eliminar un contrato activo");
        }

        try {
            logger.debug("Eliminando garantes del contrato {}", id);
            garanteRepository.deleteByContratoId(id);

            logger.debug("Eliminando notas del contrato {}", id);
            notaRepository.deleteByContratoId(id);

            logger.debug("Eliminando recibos del contrato {}", id);
            reciboRepository.deleteByContratoId(id);

            logger.debug("Eliminando contrato {}", id);
            contratoRepository.delete(contrato);

            logger.info("Contrato eliminado correctamente con ID: {}", id);

        } catch (DataIntegrityViolationException dive) {
            logger.error("Violación de integridad al eliminar contrato con ID: {}", id, dive);
            throw new RuntimeException("Violación de integridad: " + dive.getMessage());
        } catch (Exception e) {
            logger.error("Error general al eliminar contrato con ID: {}", id, e);
            throw new RuntimeException("Error general al eliminar el contrato: " + e.getMessage());
        }
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

