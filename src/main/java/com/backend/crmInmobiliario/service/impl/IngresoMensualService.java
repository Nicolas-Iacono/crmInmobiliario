package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.salida.IngresoMensualResumenDto;
import com.backend.crmInmobiliario.DTO.salida.IngresoMensualSalidaDto;
import com.backend.crmInmobiliario.controller.ContratoController;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.IngresoMensual;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.repository.IngresoMensualRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.impl.IA.EmbeddingService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class IngresoMensualService {

    private final ContratoRepository contratoRepository;
    private final IngresoMensualRepository ingresoMensualRepository;
    private final ModelMapper mapper;
    private final UsuarioRepository usuarioRepository;
    private final static Logger LOGGER = LoggerFactory.getLogger(ContratoController.class);
    private final AuthUtil authUtil;
    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    private final EmbeddingService embeddingService;


    public IngresoMensualService(AuthUtil authUtil, ContratoRepository contratoRepository,
                                 IngresoMensualRepository ingresoMensualRepository,
                                 ModelMapper mapper, UsuarioRepository usuarioRepository, EmbeddingService embeddingService) {
        this.contratoRepository = contratoRepository;
        this.ingresoMensualRepository = ingresoMensualRepository;
        this.mapper = mapper;
        this.usuarioRepository = usuarioRepository;
        this.authUtil = authUtil;
        this.embeddingService = embeddingService;
    }
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void generarIngresosDelMesActual(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        LocalDate hoy = LocalDate.now();
        int mesActual = hoy.getMonthValue();
        int anioActual = hoy.getYear();

        List<Contrato> contratosUsuario = contratoRepository.findByUsuario(usuario);

        for (Contrato contrato : contratosUsuario) {

            LocalDate fechaInicio = contrato.getFecha_inicio();
            if (fechaInicio == null || fechaInicio.isAfter(hoy)) continue;

            int anioInicio = fechaInicio.getYear();
            int mesInicio = fechaInicio.getMonthValue();

            int duracion = contrato.getDuracion() > 0 ? contrato.getDuracion() : 1;
            BigDecimal monto = BigDecimal.valueOf(
                    contrato.getMontoAlquiler() != null ? contrato.getMontoAlquiler() : 0.0
            );

            BigDecimal comisionContrato = contrato.getComisionContratoPorc() != null
                    ? contrato.getComisionContratoPorc() : BigDecimal.ZERO;

            BigDecimal comisionMensual = contrato.getComisionMensualPorc() != null
                    ? contrato.getComisionMensualPorc() : BigDecimal.ZERO;

            if (comisionContrato.compareTo(BigDecimal.valueOf(100)) > 0)
                comisionContrato = comisionContrato.divide(BigDecimal.valueOf(100));

            if (comisionMensual.compareTo(BigDecimal.valueOf(100)) > 0)
                comisionMensual = comisionMensual.divide(BigDecimal.valueOf(100));

            LocalDate fechaIter = fechaInicio;

            while (!fechaIter.isAfter(hoy)) {

                int mes = fechaIter.getMonthValue();
                int anio = fechaIter.getYear();

                if (ingresoMensualRepository.existsByContratoAndAnioAndMes(contrato, anio, mes)) {
                    fechaIter = fechaIter.plusMonths(1);
                    continue;
                }

                BigDecimal ingresoMes = monto.multiply(comisionMensual)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                BigDecimal ingresoContrato = BigDecimal.ZERO;
                if (anio == anioInicio && mes == mesInicio) {
                    ingresoContrato = monto.multiply(BigDecimal.valueOf(duracion))
                            .multiply(comisionContrato)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }

                IngresoMensual ingreso = IngresoMensual.builder()
                        .contrato(contrato)
                        .usuario(usuario)
                        .anio(anio)
                        .mes(mes)
                        .montoAlquiler(monto)
                        .porcentajeComisionContrato(comisionContrato)
                        .porcentajeComisionMensual(comisionMensual)
                        .ingresoCalculadoPorContrato(ingresoContrato)
                        .ingresoCalculadoPorMes(ingresoMes)
                        .fechaRegistro(LocalDateTime.now())
                        .build();

                ingresoMensualRepository.save(ingreso);
                fechaIter = fechaIter.plusMonths(1);
            }
        }
    }




    @Transactional(readOnly = true)
    public List<IngresoMensualSalidaDto> obtenerPorMesYAnio(int mes, int anio) {

        Long userId = authUtil.extractUserId();

        Usuario usuario = usuarioRepository.findUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<IngresoMensual> ingresos = ingresoMensualRepository.findByUsuarioAndMesAndAnio(usuario, mes, anio);

        return ingresos.stream()
                .map(ing -> IngresoMensualSalidaDto.builder()
                        .id(ing.getId())
                        .mes(ing.getMes())
                        .anio(ing.getAnio())
                        .montoAlquiler(ing.getMontoAlquiler())
                        .porcentajeComisionContrato(ing.getPorcentajeComisionContrato())
                        .porcentajeComisionMensual(ing.getPorcentajeComisionMensual())
                        .ingresoCalculadoPorContrato(ing.getIngresoCalculadoPorContrato())
                        .ingresoCalculadoPorMes(ing.getIngresoCalculadoPorMes())
                        .nombreContrato(ing.getContrato().getNombreContrato())
                        .nombreUsuario(usuario.getUsername())
                        .userId(usuario.getId())
                        .build())
                .toList();
    }

    public List<IngresoMensualResumenDto> obtenerResumenAnual(Long userId, int anio) {
        Usuario usuario = usuarioRepository.findUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Object[]> resultados = ingresoMensualRepository.obtenerTotalesAgrupadosPorMes(usuario, anio);

        return resultados.stream()
                .map(r -> new IngresoMensualResumenDto(
                        (int) r[1],                       // mes
                        (int) r[0],                       // anio (dependiendo del orden en la query)
                        (BigDecimal) r[2],                // total mensual
                        (BigDecimal) r[3]                 // total por contrato
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void generarParaContrato(Contrato contrato) {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int anio = hoy.getYear();

        Long contratoId = contrato.getId();

        // 1) Borrar cualquier ingreso que ya exista para ese contrato/mes/año
        ingresoMensualRepository.deleteByContratoIdAndAnioAndMes(contratoId, anio, mes);

        // 2) Calcular valores
        BigDecimal ingresoPorMes = contrato.getComisionMensualMonto();     // alquiler * %mensual
        BigDecimal ingresoPorContrato = BigDecimal.ZERO;

        LocalDate fi = contrato.getFecha_inicio();
        boolean esContratoDelMes = (fi != null && fi.getMonthValue() == mes && fi.getYear() == anio);
        if (esContratoDelMes) {
            ingresoPorContrato = contrato.getComisionContratoMonto();      // (alquiler*duración)*%contrato
        }

        BigDecimal montoAlquiler = BigDecimal.valueOf(
                contrato.getMontoAlquiler() != null ? contrato.getMontoAlquiler() : 0.0
        );

        BigDecimal porcContrato = contrato.getComisionContratoPorc() != null
                ? contrato.getComisionContratoPorc() : BigDecimal.ZERO;

        BigDecimal porcMensual = contrato.getComisionMensualPorc() != null
                ? contrato.getComisionMensualPorc() : BigDecimal.ZERO;

        IngresoMensual ingreso = IngresoMensual.builder()
                .contrato(contrato)
                .usuario(contrato.getUsuario())
                .mes(mes)
                .anio(anio)
                .montoAlquiler(montoAlquiler)
                .porcentajeComisionContrato(porcContrato)
                .porcentajeComisionMensual(porcMensual)
                .ingresoCalculadoPorContrato(ingresoPorContrato)
                .ingresoCalculadoPorMes(ingresoPorMes)
                .fechaRegistro(LocalDateTime.now())
                .build();

        IngresoMensual ingresoGuardado = ingresoMensualRepository.save(ingreso);

        // 3) Enviar a Supabase + embedding (no bloqueante)
        CompletableFuture.runAsync(() -> {
            try {
                String contenido = buildContenidoIngreso(ingresoGuardado);
                upsertIngresoEnSupabase(ingresoGuardado, contenido);

                List<Float> embedding = embeddingService.generarEmbedding(contenido);
                upsertIngresoEmbeddingEnSupabase(ingresoGuardado, embedding, contenido);
            } catch (Exception e) {
                LOGGER.error("⚠️ Error enviando ingreso {} a Supabase/IA: {}", ingresoGuardado.getId(), e.getMessage());
            }
        });
    }




    @Transactional
    public void regenerarIngresosFaltantes() {

        Long userId = authUtil.extractUserId();
        if (userId == null) {
            throw new RuntimeException("No se encontró el ID del usuario en el token");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDate hoy = LocalDate.now();
        YearMonth hoyYm = YearMonth.from(hoy);

        List<Contrato> contratos = contratoRepository.findByUsuario(usuario);

        for (Contrato contrato : contratos) {

            if (!contrato.isActivo()) continue; // opcional pero muy lógico

            LocalDate inicio = contrato.getFecha_inicio();
            LocalDate fin = contrato.getFecha_fin();
            if (inicio == null || fin == null) continue;

            YearMonth start = YearMonth.from(inicio);
            YearMonth end   = YearMonth.from(fin);

            // No generes más allá del mes actual
            if (end.isAfter(hoyYm)) {
                end = hoyYm;
            }

            for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
                int mes  = ym.getMonthValue();
                int anio = ym.getYear();

                boolean existe = ingresoMensualRepository
                        .existsByContratoAndAnioAndMes(contrato, anio, mes);
                // si podés, mejor: existsByContratoIdAndAnioAndMes(contrato.getId(), anio, mes)

                if (!existe) {
                    LOGGER.warn("⚠️ Ingreso faltante → contrato={} ({}) mes/año={}/{}",
                            contrato.getId(), contrato.getNombreContrato(), mes, anio);

                    generarIngresoMensual(contrato, usuario, mes, anio);
                }
            }
        }

        LOGGER.info("✅ Regeneración completa para userId={}", userId);
    }

    private void generarIngresoMensual(Contrato contrato, Usuario usuario, int mes, int anio) {

        BigDecimal montoAlquiler = BigDecimal.valueOf(
                contrato.getMontoAlquiler() != null ? contrato.getMontoAlquiler() : 0.0
        );

        BigDecimal comisionMensual = contrato.getComisionMensualPorc() != null
                ? contrato.getComisionMensualPorc() : BigDecimal.ZERO;

        BigDecimal ingresoPorMes = montoAlquiler
                .multiply(comisionMensual)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        IngresoMensual ingreso = IngresoMensual.builder()
                .contrato(contrato)
                .usuario(usuario)
                .mes(mes)
                .anio(anio)
                .montoAlquiler(montoAlquiler)
                .porcentajeComisionContrato(contrato.getComisionContratoPorc())
                .porcentajeComisionMensual(comisionMensual)
                .ingresoCalculadoPorContrato(BigDecimal.ZERO) // no se recalcula acá
                .ingresoCalculadoPorMes(ingresoPorMes)
                .fechaRegistro(LocalDateTime.now())
                .build();

        ingresoMensualRepository.save(ingreso);
    }
    @Transactional
    public void sincronizarIngresosExistentesConSupabase() {

        Long userId = authUtil.extractUserId();
        if (userId == null) {
            throw new RuntimeException("No se encontró el ID del usuario en el token");
        }

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 🔹 Traés todos los ingresos de ese usuario
        List<IngresoMensual> ingresos = ingresoMensualRepository.findByUsuario(usuario);

        if (ingresos.isEmpty()) {
            LOGGER.warn("⚠️ No hay ingresos para sincronizar en Supabase para el usuario {}", userId);
            return;
        }

        LOGGER.info("🚀 Sincronizando {} ingresos a Supabase para userId={}", ingresos.size(), userId);

        for (IngresoMensual ingreso : ingresos) {
            try {
                String contenido = buildContenidoIngreso(ingreso);

                // 👉 upsert en tabla ingresos_mensuales
                upsertIngresoEnSupabase(ingreso, contenido);

                // 👉 generar y subir embedding a ingresos_mensuales_embeddings
                List<Float> embedding = embeddingService.generarEmbedding(contenido);
                upsertIngresoEmbeddingEnSupabase(ingreso, embedding, contenido);

            } catch (Exception e) {
                LOGGER.error("❌ Error sincronizando ingreso id={} con Supabase: {}",
                        ingreso.getId(), e.getMessage());
            }
        }

        LOGGER.info("✅ Sincronización de ingresos con Supabase finalizada para userId={}", userId);
    }

    /// parte para subir embeddings a supabase

    private String buildContenidoIngreso(IngresoMensual ingreso) {
        Contrato c = ingreso.getContrato();
        Usuario u = ingreso.getUsuario();

        String nombreContrato = (c != null ? c.getNombreContrato() : "");
        String username       = (u != null ? u.getUsername() : "");

        StringBuilder sb = new StringBuilder();

        sb.append("INGRESO_MENSUAL|")
                .append("id_ingreso=").append(ingreso.getId())
                .append(" | id_contrato=").append(c != null ? c.getId() : null)
                .append(" | nombre_contrato=").append(nombreContrato)
                .append(" | usuario=").append(username)
                .append(" | user_id=").append(u != null ? u.getId() : null)
                .append(" | anio=").append(ingreso.getAnio())
                .append(" | mes=").append(ingreso.getMes())
                .append(" | monto_alquiler=").append(ingreso.getMontoAlquiler())
                .append(" | porc_comision_contrato=").append(ingreso.getPorcentajeComisionContrato())
                .append(" | porc_comision_mensual=").append(ingreso.getPorcentajeComisionMensual())
                .append(" | ingreso_por_contrato=").append(ingreso.getIngresoCalculadoPorContrato())
                .append(" | ingreso_por_mes=").append(ingreso.getIngresoCalculadoPorMes());

        return sb.toString();
    }

    private void upsertIngresoEnSupabase(IngresoMensual ingreso, String contenido) throws IOException {

        Map<String, Object> registro = Map.ofEntries(
                Map.entry("id_ingreso", ingreso.getId()),
                Map.entry("id_contrato", ingreso.getContrato().getId()),
                Map.entry("user_id", ingreso.getUsuario().getId()),
                Map.entry("username", ingreso.getUsuario().getUsername()),
                Map.entry("anio", ingreso.getAnio()),
                Map.entry("mes", ingreso.getMes()),
                Map.entry("monto_alquiler", ingreso.getMontoAlquiler()),
                Map.entry("porc_comision_contrato", ingreso.getPorcentajeComisionContrato()),
                Map.entry("porc_comision_mensual", ingreso.getPorcentajeComisionMensual()),
                Map.entry("ingreso_por_contrato", ingreso.getIngresoCalculadoPorContrato()),
                Map.entry("ingreso_por_mes", ingreso.getIngresoCalculadoPorMes()),
                Map.entry("contenido", contenido)
        );

        String json = objectMapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/ingresos_mensuales")
                .post(RequestBody.create(
                        json,
                        MediaType.parse("application/json")
                ))
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Supabase ingreso error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Ingreso upsertado en Supabase: id_ingreso={}", ingreso.getId());
        }
    }
    private void upsertIngresoEmbeddingEnSupabase(IngresoMensual ingreso,
                                                  List<Float> embedding,
                                                  String contenido) throws IOException {

        Map<String, Object> registro = Map.of(
                "id_ingreso", ingreso.getId(),
                "user_id", ingreso.getUsuario().getId(),
                "embedding", embedding,   // vector (array JSON)
                "contenido", contenido
        );

        String json = objectMapper.writeValueAsString(List.of(registro));

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/ingresos_mensuales_embeddings")
                .post(RequestBody.create(
                        json,
                        MediaType.parse("application/json")
                ))
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Supabase embedding ingreso error: " +
                        response.code() + " - " + response.body().string());
            }
            LOGGER.info("✅ Embedding de ingreso upsertado en Supabase: id_ingreso={}", ingreso.getId());
        }
    }

}
