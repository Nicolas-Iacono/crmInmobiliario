package com.backend.crmInmobiliario.service.impl.IA;

import com.backend.crmInmobiliario.DTO.IA.ContextItem;
import com.backend.crmInmobiliario.DTO.IA.IntentType;
import com.backend.crmInmobiliario.entity.*;
import com.backend.crmInmobiliario.repository.*;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatBotService {

    @Value("${supabase.url}")
    private String SUPABASE_URL;

    @Value("${supabase.service.role.key}")
    private String SUPABASE_SERVICE_ROLE_KEY;

    @Value("${openai.api.key}")
    private String OPENAI_APIKEY;

    private final Logger LOGGER = LoggerFactory.getLogger(ChatBotService.class);

    private final AuthUtil authUtil;
    private final EmbeddingService embeddingService;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(java.time.Duration.ofSeconds(15))
            .writeTimeout(java.time.Duration.ofSeconds(20))
            .readTimeout(java.time.Duration.ofSeconds(30))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final PropietarioRepository propietarioRepository;
    private final PropiedadRepository propiedadRepository;
    private final InquilinoRepository inquilinoRepository;
    private final GaranteRepository garanteRepository;
    private final ContratoRepository contratoRepository;
    private final ReciboRepository reciboRepository;
    private final IngresoMensualRepository ingresoMensualRepository;

    public ChatBotService(ReciboRepository reciboRepository, AuthUtil authUtil, EmbeddingService embeddingService, ContratoRepository contratoRepository, GaranteRepository garanteRepository, InquilinoRepository inquilinoRepository, PropiedadRepository propiedadRepository, PropietarioRepository propietarioRepository, IngresoMensualRepository ingresoMensualRepository) {
        this.authUtil = authUtil;
        this.embeddingService = embeddingService;
        this.contratoRepository = contratoRepository;
        this.garanteRepository = garanteRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.propiedadRepository = propiedadRepository;
        this.propietarioRepository = propietarioRepository;
        this.reciboRepository = reciboRepository;
        this.ingresoMensualRepository = ingresoMensualRepository;
    }



    private String responderConContextoGeneral(String prompt) {
        try {
            String respuesta = consultarLLM(prompt);

            if (respuesta == null || respuesta.isBlank())
                return "⚠️ No pude generar una respuesta útil.";

            return respuesta.trim();

        } catch (Exception e) {
            LOGGER.error("❌ Error en LLM: {}", e.getMessage());
            return "⚠️ Hubo un error al procesar la consulta.";
        }
    }
    private <T> String manejarAmbiguedad(List<T> lista, Function<T, String> formateador, String tipo) {
        if (lista.isEmpty()) return "No encontré ningún " + tipo + " con ese nombre.";
        if (lista.size() == 1) return formateador.apply(lista.get(0));

        String opciones = lista.stream().limit(5).map(formateador).collect(Collectors.joining("\n"));
        return "🤔 Encontré varios " + tipo + " con ese nombre:\n" + opciones +
                "\n¿Podrías especificar cuál te referís?";
    }


    private String normalizarTextoPersona(String texto) {
        return texto.toLowerCase()
                .replaceAll("(saldo|pendiente|recibo|recibos|cuanto|cuántos|tiene|de|del|la|el|por| )", "")
                .replace("?", "")
                .trim();
    }

    private String extraerDni(String texto) {
        String dni = texto.replaceAll("[^0-9]", "");
        return dni.isBlank() ? null : dni;
    }

    private String capitalizarPalabra(String p) {
        if (p.length() <= 1) return p.toUpperCase();
        return p.substring(0, 1).toUpperCase() + p.substring(1);
    }


    public String responderPregunta(String pregunta, String modo) throws Exception {
        Long userId = authUtil.extractUserId(); // usuario en sesión ✅

        // 1️⃣ Detectar intención del usuario
        IntentType intent = detectarIntento(pregunta);

        // 🔹 Si la intención es estructurada: responder con datos exactos (sin IA)
        switch (intent) {
            case CONTAR_PROPIEDADES:
                return responderContarPropiedades(userId);

            case LISTAR_PROPIEDADES:
                return responderListarPropiedades(userId);

            case ANALISIS_CONTRATOS_COMPLETO:
                return responderAnalisisContratosCompleto(userId, pregunta);


            case CONTAR_CONTRATOS:
                return responderContarContratos(userId);

            case LISTAR_CONTRATOS:
                return responderListarContratos(userId);

            case CONTRATOS_ACTUALIZAN_MES:
                MesAnio mesAnio = extraerMesYAnioDesdePregunta(pregunta);
                if (mesAnio == null) {
                    LocalDate hoy = LocalDate.now();
                    mesAnio = new MesAnio(hoy.getMonthValue(), hoy.getYear());
                }
                return responderContratosQueActualizanEnMes(userId, mesAnio.mes(), mesAnio.anio());

            case FECHA_ACTUALIZACION_CONTRATO:
                return responderFechaActualizacionContrato(userId, pregunta);

            case CONTRATOS_VENCEN_MES:
                MesAnio vencenMesAnio = extraerMesYAnioDesdePregunta(pregunta);
                if (vencenMesAnio == null) {
                    LocalDate hoy = LocalDate.now();
                    vencenMesAnio = new MesAnio(hoy.getMonthValue(), hoy.getYear());
                }
                return responderContratosQueVencenEnMes(userId, vencenMesAnio.mes(), vencenMesAnio.anio());

            case CONTAR_INQUILINOS:
                return responderContarInquilinos(userId);

            case LISTAR_INQUILINOS:
                return responderListarInquilinos(userId);

            case CONTAR_PROPIETARIOS:
                return responderContarPropietarios(userId);

            case LISTAR_PROPIETARIOS:
                return responderListarPropietarios(userId);

            case CONTAR_GARANTES:
                return responderContarGarantes(userId);

            case LISTAR_GARANTES:
                return responderListarGarantes(userId);

            // 🔹 Ejemplos que requieren parámetros extra
            case CONTRATOS_POR_VENCIMIENTO:
                return responderContratosPorVencimiento(userId);

            case SALDO_PENDIENTE_RECIBOS:
                String nombrePersona = extraerNombrePersona(pregunta);

                if (nombrePersona == null) {
                    // 👉 Pregunta genérica: saldo global del usuario
                    return responderSaldoPendienteGlobal(userId);
                }

                // 👉 Pregunta específica: saldo de una persona
                return responderSaldoPendiente(userId, nombrePersona);

            case GANANCIA_MES_ACTUAL:
                return responderGananciaMesActual(userId);

            case GANANCIA_MES_ESPECIFICO: {
                MesAnio ma = extraerMesYAnioDesdePregunta(pregunta);
                if (ma == null) {
                    return "No pude detectar a qué mes/año te referís. Probá con algo como: \"ganancias de comisiones de octubre 2025\".";
                }
                return responderGananciaPorMes(userId, ma.mes(), ma.anio());
            }

            case DISTRIBUCION_INGRESOS_ANUAL:
                return responderDistribucionIngresosAnual(userId, LocalDate.now().getYear());

            case TOP_CONTRATOS_INGRESOS:
                return responderTopContratosPorIngresos(userId, LocalDate.now().getYear());
            default:
                break; // pasa al flujo LLM
        }

        // 2️⃣ Si es consulta abierta, buscar contexto y usar IA
        List<ContextItem> contexto = buscarContextoEnSupabase(pregunta, userId, modo);
        String datosUsuario = construirContextoDatosUsuario(userId, pregunta);
        String prompt = construirPrompt(pregunta, contexto, datosUsuario);
        return responderConContextoGeneral(prompt);
    }

    public String responderPregunta(String pregunta) throws Exception {
        return responderPregunta(pregunta, "mixto");
    }

    private static String norm(String s){
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", ""); // sin acentos
        return n.toLowerCase().replace("?", "").trim();
    }
    private static boolean r(String p, String regex){
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(p).find();
    }

    private static boolean contieneTerminos(String texto, String... terminos) {
        if (texto == null || texto.isBlank()) return false;
        for (String termino : terminos) {
            if (texto.contains(termino)) {
                return true;
            }
        }
        return false;
    }

    private String construirContextoDatosUsuario(Long userId, String pregunta) {
        String p = norm(pregunta);
        boolean contratos = contieneTerminos(p, "contrato", "contratos", "alquiler", "locacion");
        boolean propiedades = contieneTerminos(p, "propiedad", "propiedades", "inmueble", "inmuebles");
        boolean inquilinos = contieneTerminos(p, "inquilino", "inquilinos", "arrendatario", "arrendatarios");
        boolean propietarios = contieneTerminos(p, "propietario", "propietarios", "dueno", "dueño", "duenos");
        boolean garantes = contieneTerminos(p, "garante", "garantes");
        boolean recibos = contieneTerminos(p, "recibo", "recibos", "pago", "pagos", "pendiente", "deuda", "saldo");
        boolean ingresos = contieneTerminos(p, "ingreso", "ingresos", "ganancia", "ganancias", "comision", "comisiones");

        boolean sinFiltro = !(contratos || propiedades || inquilinos || propietarios || garantes || recibos || ingresos);

        StringBuilder sb = new StringBuilder();
        sb.append("RESUMEN GENERAL:\n");
        sb.append("- Propiedades: ").append(propiedadRepository.countByUsuarioId(userId)).append("\n");
        sb.append("- Contratos: ").append(contratoRepository.countByUsuarioId(userId)).append("\n");
        sb.append("- Inquilinos: ").append(inquilinoRepository.countByUsuarioId(userId)).append("\n");
        sb.append("- Propietarios: ").append(propietarioRepository.countByUsuarioId(userId)).append("\n");
        sb.append("- Garantes: ").append(garanteRepository.countByUsuarioId(userId)).append("\n");
        sb.append("- Recibos: ").append(reciboRepository.countByContratoUsuarioId(userId)).append("\n");
        sb.append("\n");

        int limite = 8;

        if (sinFiltro || propiedades) {
            var lista = propiedadRepository.findByUsuarioId(userId);
            sb.append("PROPIEDADES:\n");
            if (lista.isEmpty()) {
                sb.append("- (sin propiedades registradas)\n\n");
            } else {
                lista.stream().limit(limite).forEach(pv ->
                        sb.append("- ").append(pv.getDireccion())
                                .append(" | ").append(pv.getLocalidad())
                                .append("\n"));
                sb.append("\n");
            }
        }

        if (sinFiltro || contratos) {
            var lista = contratoRepository.findByUsuarioIdConDetalle(userId);
            sb.append("CONTRATOS:\n");
            if (lista.isEmpty()) {
                sb.append("- (sin contratos registrados)\n\n");
            } else {
                lista.stream().limit(limite).forEach(c ->
                        sb.append("- ID ").append(c.getId())
                                .append(" | ").append(c.getNombreContrato())
                                .append(" | Inicio: ").append(c.getFecha_inicio())
                                .append(" | Fin: ").append(c.getFecha_fin())
                                .append(" | Inquilino: ")
                                .append(c.getInquilino().getNombre()).append(" ").append(c.getInquilino().getApellido())
                                .append("\n"));
                sb.append("\n");
            }
        }

        if (sinFiltro || inquilinos) {
            var lista = inquilinoRepository.findByUsuarioId(userId);
            sb.append("INQUILINOS:\n");
            if (lista.isEmpty()) {
                sb.append("- (sin inquilinos registrados)\n\n");
            } else {
                lista.stream().limit(limite).forEach(i ->
                        sb.append("- ").append(i.getNombre()).append(" ").append(i.getApellido())
                                .append(" | Tel: ").append(i.getTelefono())
                                .append(" | Email: ").append(i.getEmail())
                                .append("\n"));
                sb.append("\n");
            }
        }

        if (sinFiltro || propietarios) {
            var lista = propietarioRepository.findByUsuarioId(userId);
            sb.append("PROPIETARIOS:\n");
            if (lista.isEmpty()) {
                sb.append("- (sin propietarios registrados)\n\n");
            } else {
                lista.stream().limit(limite).forEach(pv ->
                        sb.append("- ").append(pv.getNombre()).append(" ").append(pv.getApellido())
                                .append(" | DNI: ").append(pv.getDni())
                                .append(" | CUIT: ").append(pv.getCuit())
                                .append("\n"));
                sb.append("\n");
            }
        }

        if (sinFiltro || garantes) {
            var lista = garanteRepository.findByUsuarioId(userId);
            sb.append("GARANTES:\n");
            if (lista.isEmpty()) {
                sb.append("- (sin garantes registrados)\n\n");
            } else {
                lista.stream().limit(limite).forEach(g ->
                        sb.append("- ").append(g.getNombre()).append(" ").append(g.getApellido())
                                .append(" | DNI: ").append(g.getDni())
                                .append(" | Tel: ").append(g.getTelefono())
                                .append("\n"));
                sb.append("\n");
            }
        }

        if (sinFiltro || recibos) {
            var lista = reciboRepository.findByUsuarioIdConContrato(userId);
            sb.append("RECIBOS:\n");
            if (lista.isEmpty()) {
                sb.append("- (sin recibos registrados)\n\n");
            } else {
                lista.stream().limit(limite).forEach(r ->
                        sb.append("- N° ").append(r.getNumeroRecibo())
                                .append(" | Contrato: ").append(r.getContrato().getNombreContrato())
                                .append(" | Periodo: ").append(r.getPeriodo())
                                .append(" | Monto: ").append(r.getMontoTotal())
                                .append(" | Estado: ").append(r.getEstado() ? "Pagado" : "Pendiente")
                                .append(" | Vence: ").append(r.getFechaVencimiento())
                                .append("\n"));
                sb.append("\n");
            }
        }

        if (sinFiltro || ingresos) {
            int anioActual = LocalDate.now().getYear();
            var rows = ingresoMensualRepository.totalIngresosPorContratoAnual(userId, anioActual);
            sb.append("INGRESOS POR CONTRATO (").append(anioActual).append("):\n");
            if (rows.isEmpty()) {
                sb.append("- (sin ingresos registrados en el año actual)\n\n");
            } else {
                rows.stream().limit(limite).forEach(r -> {
                    Long contratoId = (Long) r[0];
                    String nombreContrato = (String) r[1];
                    BigDecimal totalMes = (BigDecimal) r[2];
                    BigDecimal totalContrato = (BigDecimal) r[3];
                    BigDecimal total = totalMes.add(totalContrato);
                    sb.append("- Contrato ").append(contratoId)
                            .append(" | ").append(nombreContrato)
                            .append(" | Total anual: ").append(total)
                            .append("\n");
                });
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private IntentType detectarIntento(String pregunta) {
        String p = norm(pregunta);

        // ===== 🏠 PROPIEDADES =====
        // 1) LISTAR primero
        if (r(p, "\\b(lista|listar|mostra(?:me)?|ver|nombr(a|á)|dame una lista)\\b.*\\bpropiedades\\b")
                || r(p, "\\b(cuales|cu[aá]les)\\s+son\\s+mis\\s+propiedades\\b")
                || r(p, "\\bmis\\s+propiedades\\b.*\\b(lista|detalle|nombres)\\b"))
            return IntentType.LISTAR_PROPIEDADES;

        // 2) CONTAR después
        if (r(p, "\\b(cuantas|cu[aá]ntas|cantidad)\\b.*\\bpropiedades\\b")
                || r(p, "\\bpropiedades\\b.*\\b(tengo|hay)\\b")
                || r(p, "\\bpropiedades\\s+registradas\\b"))
            return IntentType.CONTAR_PROPIEDADES;

        // ===== 📑 CONTRATOS =====

        if (r(p, "\\b(actualiza|actualizan|actualizacion|actualizaciones|ajuste|ajustan)\\b")
                && r(p, "\\b(contratos?)\\b")
                && r(p, "\\b(este mes|mes actual|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre|20[0-9]{2})\\b")) {
            return IntentType.CONTRATOS_ACTUALIZAN_MES;
        }

        if (r(p, "\\b(vencen|vence|vencimiento|finaliza|finalizan|termina|terminan)\\b")
                && r(p, "\\b(contratos?)\\b")
                && r(p, "\\b(este mes|mes actual|enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre|20[0-9]{2})\\b")) {
            return IntentType.CONTRATOS_VENCEN_MES;
        }

        if (r(p, "\\bcuando\\b.*\\b(actualiza|actualizacion|ajusta)\\b.*\\bcontrato\\b")
                || r(p, "\\bactualizacion\\b.*\\bcontrato\\b")) {
            return IntentType.FECHA_ACTUALIZACION_CONTRATO;
        }

        if (r(p, "\\b(contratos?)\\b")
                && r(p, "\\b(cuales|cuáles|que|qué|todos|mis|pueden|debo|deberia|vence|vencen|renovar|actualizar|rescindir)\\b")) {
            return IntentType.ANALISIS_CONTRATOS_COMPLETO;
        }

        if (r(p, "\\b(lista|listar|mostra(?:me)?|ver|dame una lista|detalle)\\b.*\\bcontratos\\b")
                || r(p, "\\b(cuales|cu[aá]les)\\s+son\\s+mis\\s+contratos\\b"))
            return IntentType.LISTAR_CONTRATOS;

        if (r(p, "\\b(cuantos|cu[aá]ntos|cantidad)\\b.*\\bcontratos\\b")
                || r(p, "\\bcontratos\\b.*\\b(tengo|hay)\\b")
                || p.contains("contratos activos"))
            return IntentType.CONTAR_CONTRATOS;

        if (r(p, "\\b(vencen|vencimiento|por vencer|vence pronto|vencer[aá]n)\\b"))
            return IntentType.CONTRATOS_POR_VENCIMIENTO;

        if (r(p, "\\bcontrato[s]? de\\b"))
            return IntentType.CONTRATOS_POR_PERSONA;

        // ===== 👥 INQUILINOS =====
        if (r(p, "\\b(lista|listar|mostra(?:me)?|ver|dame una lista|detalle)\\b.*\\binquilinos\\b")
                || r(p, "\\b(cuales|cu[aá]les)\\s+son\\s+mis\\s+inquilinos\\b"))
            return IntentType.LISTAR_INQUILINOS;

        if (r(p, "\\b(cuantos|cu[aá]ntos|cantidad)\\b.*\\binquilinos\\b")
                || r(p, "\\binquilinos\\b.*\\b(tengo|hay)\\b"))
            return IntentType.CONTAR_INQUILINOS;

        // ===== 🏘️ PROPIETARIOS =====
        if (r(p, "\\b(lista|listar|mostra(?:me)?|ver|dame una lista|detalle)\\b.*\\bpropietarios\\b")
                || r(p, "\\b(cuales|cu[aá]les)\\s+son\\s+mis\\s+propietarios\\b"))
            return IntentType.LISTAR_PROPIETARIOS;

        if (r(p, "\\b(cuantos|cu[aá]ntos|cantidad)\\b.*\\bpropietarios\\b")
                || r(p, "\\bpropietarios\\b.*\\b(tengo|hay)\\b"))
            return IntentType.CONTAR_PROPIETARIOS;

        // ===== 🧾 GARANTES =====
        if (r(p, "\\b(lista|listar|mostra(?:me)?|ver|dame una lista|detalle)\\b.*\\bgarantes\\b")
                || r(p, "\\b(cuales|cu[aá]les)\\s+son\\s+mis\\s+garantes\\b"))
            return IntentType.LISTAR_GARANTES;

        if (r(p, "\\b(cuantos|cu[aá]ntos|cantidad)\\b.*\\bgarantes\\b")
                || r(p, "\\bgarantes\\b.*\\b(tengo|hay)\\b"))
            return IntentType.CONTAR_GARANTES;

        // ===== 💰 SALDOS =====
        if (r(p, "\\b(debe|adeuda|tiene deuda|cu[aá]nto debe|saldo\\s+pendiente)\\b")
                || r(p, "\\brecibo[s]? pendiente[s]?\\b")
                || r(p, "\\b(algun|alg[uú]n)\\s+recibo\\s+pendiente\\b")
                || r(p, "\\brecibos?\\s+sin\\s+pagar\\b")) {
            return IntentType.SALDO_PENDIENTE_RECIBOS;
        }

        // === 💰 GANANCIA / INGRESOS DEL MES ===
        if (r(p, "\\b(ganancia|ingreso[s]?|comision(es)?|comisiones)\\b")
                && r(p, "\\b(este mes|mes actual)\\b")) {
            return IntentType.GANANCIA_MES_ACTUAL;
        }

// === 📊 DISTRIBUCIÓN DE INGRESOS POR CONTRATO EN EL AÑO ===
        if (r(p, "\\b(distribu(cion|ción)|reparto|detalle)\\b")
                && r(p, "\\b(ingreso[s]?|ganancia[s]?|comision(es)?|comisiones)\\b")
                && r(p, "\\b(contrato[s]?|mis contratos)\\b")
                && r(p, "\\b(este a(n|ñ)o|año actual)\\b")) {
            return IntentType.DISTRIBUCION_INGRESOS_ANUAL;
        }

// === 🔝 CONTRATOS QUE MÁS INGRESO GENERAN ===
        if (r(p, "\\b(contrato[s]?|mis contratos)\\b")
                && r(p, "\\b(m[aá]s|mayor|top)\\b")
                && r(p, "\\b(ingreso[s]?|ganancia[s]?|comision(es)?|comisiones)\\b")) {
            return IntentType.TOP_CONTRATOS_INGRESOS;
        }

// === 💰 GANANCIA / INGRESOS DE UN MES ESPECÍFICO ===
        if (r(p, "\\b(ganancia[s]?|ingreso[s]?|comision(es)?|comisiones)\\b")
                && r(p, "\\b(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre)\\b")) {
            return IntentType.GANANCIA_MES_ESPECIFICO;
        }
        return IntentType.CONSULTA_GENERAL;
    }

    private String responderContratosQueActualizanEnMes(Long userId, int mes, int anio) {
        List<Contrato> contratos = contratoRepository.findByUsuarioIdConDetalle(userId);
        List<Contrato> coinciden = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Contrato contrato : contratos) {
            if (actualizaEnMes(contrato, mes, anio)) {
                coinciden.add(contrato);
            }
        }

        if (coinciden.isEmpty()) {
            return "En " + nombreMes(mes) + " de " + anio + " no hay contratos con actualización.";
        }

        StringBuilder sb = new StringBuilder("En " + nombreMes(mes) + " de " + anio
                + " se actualizan " + coinciden.size() + " contrato(s):\n\n");

        coinciden.forEach(c -> {
            LocalDate fecha = primeraActualizacionEnMes(c, mes, anio);
            sb.append("• ").append(c.getNombreContrato())
                    .append(" — fecha de actualización: ").append(fecha != null ? fecha.format(formatter) : "sin fecha")
                    .append("\n");
        });

        return sb.toString();
    }

    private String responderFechaActualizacionContrato(Long userId, String pregunta) {
        String nombre = extraerNombreContratoDesdePregunta(pregunta);
        if (nombre == null || nombre.isBlank()) {
            return "Necesito el nombre o ID del contrato para indicar su actualización.";
        }

        String idTexto = nombre.replaceAll("[^0-9]", "");
        if (!idTexto.isBlank()) {
            try {
                Long id = Long.parseLong(idTexto);
                Contrato contrato = contratoRepository.findByIdAndUsuarioId(id, userId).orElse(null);
                if (contrato == null) {
                    return "No encontré un contrato con ese ID.";
                }
                return responderFechaActualizacionContrato(contrato);
            } catch (NumberFormatException ignored) {
                // continúa con búsqueda por nombre
            }
        }

        List<Contrato> contratos = contratoRepository.findByNombreContratoContainingIgnoreCaseAndUsuarioIdConDetalle(nombre, userId);
        if (contratos.isEmpty()) {
            return "No encontré contratos con ese nombre.";
        }
        if (contratos.size() > 1) {
            String opciones = contratos.stream()
                    .limit(5)
                    .map(c -> "• " + c.getNombreContrato()
                            + " | Inicio: " + c.getFecha_inicio()
                            + " | Fin: " + c.getFecha_fin())
                    .collect(Collectors.joining("\n"));
            return "Encontré varios contratos con ese nombre:\n" + opciones
                    + "\n¿Podés aclarar la dirección o la fecha de inicio?";
        }

        return responderFechaActualizacionContrato(contratos.get(0));
    }

    private String responderFechaActualizacionContrato(Contrato contrato) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate proxima = proximaActualizacion(contrato, LocalDate.now());
        if (proxima == null) {
            return "El contrato " + contrato.getNombreContrato() + " no tiene una actualización programada.";
        }

        return "El contrato " + contrato.getNombreContrato()
                + " se actualiza el " + proxima.format(formatter) + ".";
    }

    private boolean actualizaEnMes(Contrato contrato, int mes, int anio) {
        return primeraActualizacionEnMes(contrato, mes, anio) != null;
    }

    private LocalDate primeraActualizacionEnMes(Contrato contrato, int mes, int anio) {
        List<LocalDate> fechas = calcularFechasActualizacion(contrato);
        return fechas.stream()
                .filter(f -> f.getYear() == anio && f.getMonthValue() == mes)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    private LocalDate proximaActualizacion(Contrato contrato, LocalDate desde) {
        List<LocalDate> fechas = calcularFechasActualizacion(contrato);
        return fechas.stream()
                .filter(f -> !f.isBefore(desde))
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    private List<LocalDate> calcularFechasActualizacion(Contrato contrato) {
        List<LocalDate> fechas = new ArrayList<>();
        if (contrato == null || contrato.getFecha_inicio() == null || contrato.getFecha_fin() == null) {
            return fechas;
        }

        int meses = contrato.getActualizacion();
        if (meses <= 0) {
            return fechas;
        }

        LocalDate fecha = contrato.getFecha_inicio().plusMonths(meses);
        LocalDate fin = contrato.getFecha_fin();
        while (!fecha.isAfter(fin)) {
            fechas.add(fecha);
            fecha = fecha.plusMonths(meses);
        }
        return fechas;
    }

    private String extraerNombreContratoDesdePregunta(String pregunta) {
        if (pregunta == null) return null;
        String texto = pregunta.toLowerCase()
                .replace("¿", "")
                .replace("?", "")
                .replaceAll("(cuando|se|actualiza|actualizacion|ajusta|el|la|los|las|contrato|contratos|de)", "")
                .trim();
        return texto.isBlank() ? null : texto;
    }

    private String responderContratosQueVencenEnMes(Long userId, int mes, int anio) {
        List<Contrato> contratos = contratoRepository.findByUsuarioIdConDetalle(userId);
        List<Contrato> coinciden = contratos.stream()
                .filter(c -> c.getFecha_fin() != null
                        && c.getFecha_fin().getYear() == anio
                        && c.getFecha_fin().getMonthValue() == mes)
                .collect(Collectors.toList());

        if (coinciden.isEmpty()) {
            return "En " + nombreMes(mes) + " de " + anio + " no hay contratos que venzan.";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder("En " + nombreMes(mes) + " de " + anio
                + " vencen " + coinciden.size() + " contrato(s):\n\n");

        coinciden.forEach(c -> sb.append("• ").append(c.getNombreContrato())
                .append(" — fin: ").append(c.getFecha_fin().format(formatter))
                .append(" | inquilino: ").append(c.getInquilino().getNombre())
                .append(" ").append(c.getInquilino().getApellido())
                .append("\n"));

        return sb.toString();
    }


    // ✅ Usa RPC match_contracts + match_codigo_civil
    private List<ContextItem> buscarContextoEnSupabase(String pregunta, Long userId, String modo) throws Exception {
        LOGGER.info("⚙️ Generando embedding de la pregunta...");
        List<Float> embedding = embeddingService.generarEmbedding(pregunta);
        List<ContextItem> contexto = new java.util.ArrayList<>();

        // ⚖️ SOLO LEYES
        if ("legales".equalsIgnoreCase(modo)) {
            Map<String, Object> body = Map.of(
                    "query_embedding", embedding,
                    "match_threshold", 0.25,
                    "match_count", 8
            );

            Request req = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/rpc/match_codigo_civil")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                    .post(RequestBody.create(mapper.writeValueAsString(body), MediaType.parse("application/json")))
                    .build();

            try (Response res = client.newCall(req).execute()) {
                if (res.isSuccessful() && res.body() != null) {
                    contexto.addAll(mapper.readValue(res.body().string(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<ContextItem>>() {}));
                }
            }
            LOGGER.info("📚 Contexto legal cargado: {} artículos", contexto.size());
            return contexto;
        }

        // 💼 SOLO DATOS
        if ("datos".equalsIgnoreCase(modo)) {
            Map<String, Object> body = Map.of(
                    "user_id", userId,
                    "query_embedding", embedding,
                    "match_threshold", 0.2,
                    "match_count", 6
            );

            Request req = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/rpc/match_contracts")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                    .post(RequestBody.create(mapper.writeValueAsString(body), MediaType.parse("application/json")))
                    .build();

            try (Response res = client.newCall(req).execute()) {
                if (res.isSuccessful() && res.body() != null) {
                    contexto.addAll(mapper.readValue(res.body().string(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<ContextItem>>() {}));
                }
            }
            LOGGER.info("📚 Contexto datos cargado: {} registros", contexto.size());
            return contexto;
        }

        // ⚖️💼 MODO MIXTO (por defecto)
        Map<String, Object> bodyUser = Map.of(
                "user_id", userId,
                "query_embedding", embedding,
                "match_threshold", 0.2,
                "match_count", 5
        );

        Map<String, Object> bodyLegal = Map.of(
                "query_embedding", embedding,
                "match_threshold", 0.25,
                "match_count", 5
        );

        Request reqUser = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/rpc/match_contracts")
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .post(RequestBody.create(mapper.writeValueAsString(bodyUser), MediaType.parse("application/json")))
                .build();

        Request reqLegal = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/rpc/match_codigo_civil")
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", SUPABASE_SERVICE_ROLE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_SERVICE_ROLE_KEY)
                .post(RequestBody.create(mapper.writeValueAsString(bodyLegal), MediaType.parse("application/json")))
                .build();

        try (Response resLegal = client.newCall(reqLegal).execute();
             Response resUser = client.newCall(reqUser).execute()) {

            if (resLegal.isSuccessful() && resLegal.body() != null)
                contexto.addAll(0, mapper.readValue(resLegal.body().string(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<ContextItem>>() {}));

            if (resUser.isSuccessful() && resUser.body() != null)
                contexto.addAll(mapper.readValue(resUser.body().string(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<ContextItem>>() {}));
        }

        LOGGER.info("📚 Contexto combinado ({} fragmentos) — modo: {}", contexto.size(), modo);
        return contexto;
    }


    private String construirPrompt(String pregunta, List<ContextItem> contexto, String datosUsuario) {
        StringBuilder ctx = new StringBuilder();

        LocalDate hoy = LocalDate.now();
        String fechaHoy = hoy.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy"));

        ctx.append("FECHA_ACTUAL: ").append(fechaHoy).append("\n\n");

        if (contexto.isEmpty()) {
            ctx.append("No hay información contextual disponible.\n");
        } else {
            // 🔍 Clasificar contexto entre artículos legales y datos inmobiliarios
            ctx.append("=== CONTEXTO LEGAL (Código Civil y Comercial Argentino) ===\n");
            contexto.stream()
                    .filter(c -> c.getContenido().toLowerCase().contains("artículo"))
                    .limit(7)
                    .forEach(c -> {
                        Double score = c.getScore();
                        String contenido = c.getContenido().replace("%", "%%");
                        ctx.append("• (").append(String.format("%.2f", score)).append(") ").append(contenido).append("\n\n");
                    });

            ctx.append("=== CONTEXTO INMOBILIARIO (Datos del usuario) ===\n");
            contexto.stream()
                    .filter(c -> !c.getContenido().toLowerCase().contains("artículo"))
                    .limit(7)
                    .forEach(c -> {
                        Double score = c.getScore();
                        String contenido = c.getContenido().replace("%", "%%");
                        ctx.append("• (").append(String.format("%.2f", score)).append(") ").append(contenido).append("\n\n");
                    });
        }

        String preguntaSegura = pregunta.replace("%", "%%");

        return """
Sos un asistente legal e inmobiliario avanzado de la plataforma Tuinmo.

REGLAS DE RESPUESTA:
- Tenés acceso a la fecha actual (FECHA_ACTUAL) y a dos fuentes de contexto:
  1️⃣ Código Civil y Comercial Argentino (CCyC)
  2️⃣ Información de la inmobiliaria (contratos, propiedades, recibos, usuarios, etc.)
- Prioridad: las leyes del CCyC tienen mayor peso interpretativo.
- La Ley 27.551 de Alquileres fue derogada por el DNU 70/2023. No debe mencionarse ni aplicarse.
- Todas las consultas sobre alquileres se responden exclusivamente con el Código Civil y Comercial Argentino (CCyC).
- El régimen actual es de libertad contractual: las partes pueden pactar plazos, actualizaciones y condiciones, salvo límites generales del CCyC.
- Si el usuario pide una explicación sobre “leyes de alquileres”, aclarar que hoy rige solo el CCyC y los contratos entre partes.
- Si hay artículos legales en el contexto, citá explícitamente su número (ejemplo: “Art. 1198 CCyC”).
- Luego, aplicá esos principios a los contratos o datos del usuario.
- Si la ley no aborda directamente el caso, explicá la limitación y respondé con lógica profesional.
- Si se pide redactar un documento, estructuralo por cláusulas.
- No inventes artículos ni datos fuera del contexto.
- Si hay varios artículos relevantes, resumí los más importantes.
- Si te preguntan “qué día es hoy”, respondé usando FECHA_ACTUAL.
- Mantené siempre un tono profesional, claro y jurídico argentino.
- Si falta un dato específico del usuario, pedí una aclaración concreta en una sola pregunta.

CONTEXTO:
%s

DATOS DEL USUARIO:
%s

PREGUNTA:
%s

RESPUESTA:
""".formatted(ctx.toString(), datosUsuario, preguntaSegura);
    }



    private String consultarLLM(String prompt) {

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content",
                                "Sos un asistente legal e inmobiliario de la plataforma Tuinmo. " +
                                        "Recordá que la Ley 27.551 de Alquileres fue derogada por el DNU 70/2023 y no debe mencionarse ni aplicarse. " +
                                        "Los alquileres hoy se rigen únicamente por el Código Civil y Comercial Argentino (CCyC) y por la libertad contractual entre partes. " +
                                        "Siempre respondé basándote en el CCyC y en los principios de autonomía de la voluntad, buena fe contractual (Art. 961 CCyC) y reglas generales de los contratos. " +
                                        "La fecha actual es: " +
                                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy")) +
                                        ". " +
                                        "Si falta información para responder con precisión, pedí una aclaración concreta."
                        ),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.2
        );

        try {
            String json = mapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + OPENAI_APIKEY)
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    throw new IOException("OpenAI Error: " +
                            response.code() + " - " + response.body().string());
                }

                JsonNode res = mapper.readTree(response.body().string());

                return res.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            LOGGER.error("❌ ERROR consultando OpenAI: {}", e.getMessage());
            return "⚠️ La IA no pudo responder ahora. Probá de nuevo en unos minutos.";
        }
    }





    // ✅ Propiedades ---------------------------

    private String responderContarPropiedades(Long userId) {
        int count = propiedadRepository.countByUsuarioId(userId);
        return "📌 Actualmente tenés **" + count + " propiedades** cargadas.";
    }

    private String responderListarPropiedades(Long userId) {
        var propiedades = propiedadRepository.findByUsuarioId(userId);

        if (propiedades.isEmpty())
            return "⚠️ No tenés propiedades registradas todavía.";

        StringBuilder sb = new StringBuilder("📍 Tus propiedades:\n");
        propiedades.forEach(p ->
                sb.append("• ").append(p.getDireccion())
                        .append(" — ").append(p.getLocalidad())
                        .append("\n")
        );
        return sb.toString();
    }

// ✅ Contratos ---------------------------
private String construirContextoContratosCompletos(Long userId) {
    List<Contrato> contratos = contratoRepository.findByUsuarioIdConDetalle(userId);

    if (contratos.isEmpty()) {
        return "El usuario no tiene contratos registrados.";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("LISTA_COMPLETA_DE_CONTRATOS:\n");

    contratos.forEach(c -> {
        sb.append(String.format(
                "- ID %d | %s | Inicio: %s | Fin: %s | Inquilino: %s %s | Propiedad: %s\n",
                c.getId(),
                c.getNombreContrato(),
                c.getFecha_inicio(),
                c.getFecha_fin(),
                c.getInquilino().getNombre(),
                c.getInquilino().getApellido(),
                c.getPropiedad().getDireccion()
        ));
    });

    sb.append("\n");

    return sb.toString();
}
    private String explicarGananciaMesConIA(BigDecimal total, int mes, int anio) {
        String prompt = """
    Tengo los ingresos por comisiones de un usuario inmobiliario.

    - Año: %d
    - Mes: %s (%d)
    - Total de comisiones cobradas en el mes: %s

    Redactá una respuesta corta (máximo 3 frases) en español rioplatense,
    explicándole cuánto ganó en comisiones ese mes y sumá
    una frase de motivación muy breve.
    """.formatted(anio, nombreMes(mes), mes, total.toPlainString());

        String respuesta = consultarLLM(prompt);
        if (respuesta == null || respuesta.isBlank()) {
            return "En " + nombreMes(mes) + " de " + anio +
                    " tus comisiones suman aproximadamente $" + total.toPlainString() + ".";
        }
        return respuesta.trim();
    }


    private String responderGananciaMesActual(Long userId) {
        LocalDate hoy = LocalDate.now();
        return responderGananciaPorMes(userId, hoy.getMonthValue(), hoy.getYear());
    }


    private String responderGananciaPorMes(Long userId, int mes, int anio) {
        BigDecimal totalMes = ingresoMensualRepository.totalIngresosPorMes(userId, anio, mes);
        if (totalMes == null) totalMes = BigDecimal.ZERO;

        if (totalMes.compareTo(BigDecimal.ZERO) == 0) {
            return "En " + nombreMes(mes) + " de " + anio + " no tenés comisiones registradas.";
        }

        return explicarGananciaMesConIA(totalMes, mes, anio);
    }

    private String responderDistribucionIngresosAnual(Long userId, int anio) {
        List<Object[]> rows = ingresoMensualRepository.totalIngresosPorContratoAnual(userId, anio);

        if (rows.isEmpty()) {
            return "En " + anio + " todavía no tenés ingresos registrados por contratos.";
        }

        // armamos un pequeño "dataset" de texto para pasarle a la IA
        StringBuilder datos = new StringBuilder();
        BigDecimal total = BigDecimal.ZERO;

        for (Object[] r : rows) {
            Long contratoId = (Long) r[0];
            String nombreContrato = (String) r[1];
            BigDecimal totalMes = (BigDecimal) r[2];
            BigDecimal totalContrato = (BigDecimal) r[3];

            BigDecimal totalContratoAnual = totalMes.add(totalContrato);
            total = total.add(totalContratoAnual);

            datos.append("- Contrato ID ").append(contratoId)
                    .append(" (").append(nombreContrato).append(")")
                    .append(": total anual = ").append(totalContratoAnual.toPlainString())
                    .append("\n");
        }

        String prompt = """
    Tengo los ingresos anuales por contratos de una inmobiliaria.

    Año: %d
    Total general del año (suma de todos los contratos): %s

    Detalle por contrato:
    %s

    Explicá de forma clara:
    1) Cuánto es el total del año.
    2) Qué contratos son los que más aportan (mencioná los 3 primeros si hay muchos).
    3) Hacé un mini análisis de distribución (ej: "la mayor parte viene de...").
    Mantené la respuesta en 5–7 líneas máximo.
    """.formatted(anio, total.toPlainString(), datos);

        return consultarLLM(prompt);
    }
    private String responderTopContratosPorIngresos(Long userId, int anio) {
        List<Object[]> rows = ingresoMensualRepository.totalIngresosPorContratoAnual(userId, anio);

        if (rows.isEmpty()) {
            return "En " + anio + " no hay contratos con ingresos registrados todavía.";
        }

        StringBuilder sb = new StringBuilder("📊 Contratos que más ingreso te generaron en ")
                .append(anio).append(":\n\n");

        int limit = Math.min(5, rows.size());
        for (int i = 0; i < limit; i++) {
            Object[] r = rows.get(i);
            String nombreContrato = (String) r[1];
            BigDecimal totalMes = (BigDecimal) r[2];
            BigDecimal totalContrato = (BigDecimal) r[3];
            BigDecimal totalContratoAnual = totalMes.add(totalContrato);

            sb.append(i + 1).append(". ")
                    .append(nombreContrato)
                    .append(" — ingreso total: $")
                    .append(totalContratoAnual.toPlainString())
                    .append("\n");
        }

        return sb.toString();
    }

    private record MesAnio(int mes, int anio) {}

    private MesAnio extraerMesYAnioDesdePregunta(String pregunta) {
        String p = norm(pregunta); // ya sin acentos, en minúsculas

        int anio = LocalDate.now().getYear();

        // Buscar año explícito (ej: "2024", "2025")
        var matcherYear = Pattern.compile("(20[0-9]{2})").matcher(p);
        if (matcherYear.find()) {
            anio = Integer.parseInt(matcherYear.group(1));
        }

        int mes = -1;
        var matcherMes = Pattern.compile(
                "enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|setiembre|octubre|noviembre|diciembre"
        ).matcher(p);

        if (matcherMes.find()) {
            String m = matcherMes.group();
            switch (m) {
                case "enero"      -> mes = 1;
                case "febrero"    -> mes = 2;
                case "marzo"      -> mes = 3;
                case "abril"      -> mes = 4;
                case "mayo"       -> mes = 5;
                case "junio"      -> mes = 6;
                case "julio"      -> mes = 7;
                case "agosto"     -> mes = 8;
                case "septiembre", "setiembre" -> mes = 9;
                case "octubre"    -> mes = 10;
                case "noviembre"  -> mes = 11;
                case "diciembre"  -> mes = 12;
            }
        }

        if (mes == -1) return null;
        return new MesAnio(mes, anio);
    }

    private String nombreMes(int mes) {
        return switch (mes) {
            case 1 -> "enero";
            case 2 -> "febrero";
            case 3 -> "marzo";
            case 4 -> "abril";
            case 5 -> "mayo";
            case 6 -> "junio";
            case 7 -> "julio";
            case 8 -> "agosto";
            case 9 -> "septiembre";
            case 10 -> "octubre";
            case 11 -> "noviembre";
            case 12 -> "diciembre";
            default -> "mes desconocido";
        };
    }

    private String responderAnalisisContratosCompleto(Long userId, String pregunta) throws Exception {

        // 1️⃣ Contexto legal relevante
        List<ContextItem> contextoLegal = buscarContextoEnSupabase(pregunta, userId, "legales");

        // 2️⃣ Lista completa de contratos
        String contextoContratos = construirContextoContratosCompletos(userId);

        StringBuilder legales = new StringBuilder();
        contextoLegal.forEach(c -> legales.append("• ").append(c.getContenido()).append("\n"));

        String prompt = """
Sos un asistente legal e inmobiliario experto de la plataforma Tuinmo.

Reglas legales:
- La Ley 27.551 está derogada por el DNU 70/2023.
- Todo se rige por el Código Civil y Comercial Argentino (CCyC).
- Aplicá artículos sobre contratos, obligaciones, plazos, buena fe, etc.
- Si corresponde, citá artículos: “Art. XXXX CCyC”.

FECHA_ACTUAL: %s

=== CONTEXTO LEGAL ===
%s

=== CONTRATOS DEL USUARIO (TODOS) ===
%s

PREGUNTA:
%s

INSTRUCCIONES:
- Leé y analizá TODOS los contratos, no un subconjunto.
- Compará fechas, plazos, renovaciones, rescisión, vencimientos o lo que se pida.
- No inventes datos. Usá únicamente el contexto.
""".formatted(
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                legales,
                contextoContratos,
                pregunta
        );

        return responderConContextoGeneral(prompt);
    }





    private String responderContarContratos(Long userId) {
        int count = contratoRepository.countByUsuarioId(userId);
        return "📑 Tenés **" + count + " contratos activos**.";
    }

    private String responderListarContratos(Long userId) {
        var contratos = contratoRepository.findByUsuarioId(userId);

        if (contratos.isEmpty())
            return "⚠️ No encontré contratos en tu cuenta.";

        StringBuilder sb = new StringBuilder("📑 Contratos:\n");
        contratos.forEach(c ->
                sb.append("• ").append(c.getNombreContrato())
                        .append(" — ").append(c.getFecha_fin())
                        .append("\n")
        );
        return sb.toString();
    }
    private String extraerContrato(String pregunta) {
        Long userId = authUtil.extractUserId();
        String texto = pregunta.toLowerCase();

        String idTexto = texto.replaceAll("[^0-9]", "");

        // 🔍 Buscar por ID si hay un número
        if (!idTexto.isBlank()) {
            try {
                Long id = Long.parseLong(idTexto);
                return contratoRepository.findByIdAndUsuarioId(id, userId)
                        .map(Contrato::getNombreContrato)
                        .orElse(null);
            } catch (NumberFormatException ignored) {}
        }

        // 🔍 Buscar por texto
        List<Contrato> resultados =
                contratoRepository.findByNombreContratoContainingIgnoreCaseAndUsuarioId(texto, userId);

        if (resultados.isEmpty()) return null;
        if (resultados.size() == 1)
            return resultados.get(0).getNombreContrato();

        return "⚠ Hay múltiples contratos que coinciden. Especifica ID o nombre exacto.";
    }



// ✅ Contratos por vencimiento ----------

    private String responderContratosPorVencimiento(Long userId) {
        var proximos = obtenerVencimientosProximos(userId);

        if (proximos.isEmpty())
            return "✅ Ningún contrato se encuentra próximo a vencer.";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        StringBuilder sb = new StringBuilder("⏳ Contratos próximos a vencer:\n");
        proximos.forEach(c -> sb.append("• ")
                .append(c.getNombreContrato())
                .append(" — Vence: ")
                .append(c.getFecha_fin().format(formatter))
                .append("\n")
        );
        return sb.toString();
    }
    public List<Contrato> obtenerVencimientosProximos(Long userId) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(60);
        return contratoRepository.findVencimientosProximos(userId, hoy, limite);
    }

// ✅ Inquilinos --------------------------

    private String responderContarInquilinos(Long userId) {
        int count = inquilinoRepository.countByUsuarioId(userId);
        return "👥 Tenés **" + count + " inquilinos** registrados.";
    }

    private String responderListarInquilinos(Long userId) {
        var lista = inquilinoRepository.findByUsuarioId(userId);

        if (lista.isEmpty())
            return "⚠️ No hay inquilinos cargados.";

        StringBuilder sb = new StringBuilder("👥 Inquilinos:\n");
        lista.forEach(q ->
                sb.append("• ").append(q.getNombre()).append(" ").append(q.getApellido())
                        .append(" — ").append(q.getTelefono()).append("\n")
        );
        return sb.toString();
    }

    private String extraerNombrePersona(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) return null;

        Long userId = authUtil.extractUserId();

        String texto = pregunta.toLowerCase()
                .replaceAll("(saldo|pendiente|recibo|recibos|adeuda|debe|tiene|cuanto|cuántos|cuántas|\\?)", "")
                .trim();

        // Si no queda nada después de limpiar, es una pregunta global
        if (texto.isBlank()) return null;

        // Intentar buscar por DNI
        String dniMatch = texto.replaceAll("[^0-9]", "");
        if (!dniMatch.isBlank()) {
            return buscarInquilinoPorDni(dniMatch, userId);
        }

        // Intentar buscar por correo
        if (texto.contains("@")) {
            return buscarInquilinoPorEmail(texto, userId);
        }

        // Nombre normalizado
        String nombreCompleto = Arrays.stream(texto.split("\\s+"))
                .filter(p -> p.length() > 1)
                .map(this::capitalizar)
                .collect(Collectors.joining(" "));

        if (nombreCompleto.isBlank()) return null;

        return buscarInquilinoPorNombre(nombreCompleto, userId);
    }
    private String buscarInquilinoPorNombre(String nombreCompleto, Long userId) {
        List<Inquilino> coincidencias =
                inquilinoRepository.buscarPorNombreOApellido(nombreCompleto, userId);

        if (coincidencias.isEmpty()) {
            return null; // no encontrado
        }

        if (coincidencias.size() > 1) {
            // si querés, más adelante podés manejar ambigüedad
            return null;
        }

        Inquilino i = coincidencias.get(0);
        return i.getNombre() + " " + i.getApellido(); // solo el nombre, sin texto extra
    }
    private String responderSaldoPendienteGlobal(Long userId) {
        BigDecimal saldo = reciboRepository.findSaldoPendienteGlobal(userId);
        long cantidad = reciboRepository.countPendientesGlobal(userId);

        if (saldo == null || saldo.compareTo(BigDecimal.ZERO) == 0) {
            return "✅ No tenés recibos pendientes.";
        }

        return "💰 Tenés " + cantidad + " recibo(s) pendiente(s) por un total de $" + saldo;
    }

    private String buscarInquilinoPorDni(String dni, Long userId) {
        return inquilinoRepository.findByDniAndUsuarioId(dni, userId)
                .map(i -> i.getNombre() + " " + i.getApellido())
                .orElse(null);
    }

    private String buscarInquilinoPorEmail(String email, Long userId) {
        return inquilinoRepository.findByEmailIgnoreCaseAndUsuarioId(email, userId)
                .map(i -> i.getNombre() + " " + i.getApellido())
                .orElse(null);
    }

    private String capitalizar(String palabra) {
        if (palabra.length() < 2) return palabra.toUpperCase();
        return palabra.substring(0, 1).toUpperCase() + palabra.substring(1).toLowerCase();
    }








// ✅ Propietarios -----------------------

    private String responderContarPropietarios(Long userId) {
        int count = propietarioRepository.countByUsuarioId(userId);
        return "🏠 Tenés **" + count + " propietarios** en tu cartera.";
    }

    private String responderListarPropietarios(Long userId) {
        var lista = propietarioRepository.findByUsuarioId(userId);

        if (lista.isEmpty())
            return "⚠️ No hay propietarios cargados.";

        StringBuilder sb = new StringBuilder("🏠 Propietarios:\n");
        lista.forEach(p ->
                sb.append("• ").append(p.getNombre()).append(" ").append(p.getApellido())
                        .append(" — CUIT ").append(p.getCuit()).append("\n")
        );
        return sb.toString();
    }


    private String extraerPropietario(String pregunta) {
        Long userId = authUtil.extractUserId();
        String texto = normalizarTextoPersona(pregunta);

        String dni = extraerDni(texto);
        if (dni != null) {
            return propietarioRepository.findByDniAndUsuarioId(dni, userId)
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .orElse(null);
        }

        if (texto.contains("@")) {
            return propietarioRepository.findByEmailIgnoreCaseAndUsuarioId(texto, userId)
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .orElse(null);
        }

        return buscarPropietarioPorNombre(texto, userId);
    }

    private String buscarPropietarioPorNombre(String nombreCompleto, Long userId) {
        List<Propietario> coincidencias =
                propietarioRepository.findByNombreLikeIgnoreCaseOrApellidoLikeIgnoreCaseAndUsuarioId(
                        nombreCompleto, nombreCompleto, userId
                );

        return manejarAmbiguedad(
                coincidencias,
                p -> p.getNombre() + " " + p.getApellido() + " (DNI: " + p.getDni() + ")",
                "propietario"
        );
    }

// ✅ Garantes --------------------------

    private String responderContarGarantes(Long userId) {
        int count = garanteRepository.countByUsuarioId(userId);
        return "🧾 Tenés **" + count + " garantes** asociados.";
    }

    private String responderListarGarantes(Long userId) {
        var lista = garanteRepository.findByUsuarioId(userId);

        if (lista.isEmpty())
            return "⚠️ No tenés garantes registrados.";

        StringBuilder sb = new StringBuilder("🧾 Garantes:\n");
        lista.forEach(g ->
                sb.append("• ").append(g.getNombre()).append(" ").append(g.getApellido())
                        .append(" — DNI ").append(g.getDni()).append("\n")
        );
        return sb.toString();
    }

    private String extraerGarante(String pregunta) {
        if (pregunta == null || pregunta.isBlank()) return null;

        Long userId = authUtil.extractUserId();
        String texto = normalizarTextoPersona(pregunta);

        // 🔍 Intentar DNI
        String dni = extraerDni(texto);
        if (dni != null) {
            return garanteRepository.findByDniAndUsuarioId(dni, userId)
                    .map(g -> g.getNombre() + " " + g.getApellido())
                    .orElse("⚠️ No encontré ningún garante con ese DNI.");
        }

        // 🔍 Intentar Email
        if (texto.contains("@")) {
            return garanteRepository.findByEmailIgnoreCaseAndUsuarioId(texto, userId)
                    .map(g -> g.getNombre() + " " + g.getApellido())
                    .orElse("⚠️ No encontré ningún garante con ese correo electrónico.");
        }

        // 🔍 Intentar Nombre/Apellido → usa tu método genérico
        return buscarGarantePorNombre(texto, userId);
    }


    private String buscarGarantePorNombre(String nombreCompleto, Long userId) {
        List<Garante> coincidencias =
                garanteRepository.findByNombreLikeIgnoreCaseOrApellidoLikeIgnoreCaseAndUsuarioId(
                        nombreCompleto, nombreCompleto, userId
                );

        return manejarAmbiguedad(
                coincidencias,
                g -> g.getNombre() + " " + g.getApellido() + " (DNI: " + g.getDni() + ")",
                "garante"
        );
    }



// ✅ Saldos pendientes por persona -------

    private String responderSaldoPendiente(Long userId, String nombrePersona) {
        if (nombrePersona == null || nombrePersona.isBlank()) {
            return "Necesito que me indiques a quién te referís (nombre y apellido o DNI).";
        }

        List<Recibo> pendientes = reciboRepository.findPendientesByPersona(userId, nombrePersona);

        if (pendientes.isEmpty()) {
            return "✅ " + nombrePersona + " no tiene recibos pendientes.";
        }

        BigDecimal saldoTotal = pendientes.stream()
                .map(Recibo::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        StringBuilder sb = new StringBuilder();
        sb.append("💰 ").append(nombrePersona)
                .append(" tiene ").append(pendientes.size()).append(" recibo(s) pendiente(s).\n\n")
                .append("Detalle:\n");

        pendientes.forEach(r -> sb.append("- Recibo N° ").append(r.getNumeroRecibo())
                .append(" | Periodo: ").append(r.getPeriodo())
                .append(" | Monto: $").append(r.getMontoTotal())
                .append(" | Vencimiento: ").append(r.getFechaVencimiento().format(fmt))
                .append("\n"));

        sb.append("\nSaldo total pendiente: $").append(saldoTotal);

        return sb.toString();
    }



}
