package com.backend.crmInmobiliario.service.impl.vapi;

import com.backend.crmInmobiliario.entity.Lead;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.LeadRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class VapiLeadService {

    private static final String END_OF_CALL_REPORT = "end-of-call-report";
    private static final Pattern USER_ID_PATTERN = Pattern.compile(".*?(\\d+)$");

    private final LeadRepository leadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processWebhook(JsonNode payload) {
        JsonNode eventNode = unwrapMessageNode(payload);
        if (eventNode == null || eventNode.isNull()) {
            log.warn("Webhook de Vapi sin payload usable");
            return;
        }

        String type = textValue(eventNode.path("type"));
        if (!END_OF_CALL_REPORT.equals(type)) {
            return;
        }

        String callId = textValue(eventNode.path("call").path("id"));
        if (callId == null || callId.isBlank()) {
            log.warn("Webhook end-of-call-report sin call.id");
            return;
        }

        if (leadRepository.existsByCallId(callId)) {
            return;
        }

        JsonNode structuredData = eventNode.path("analysis").path("structuredData");
        if (structuredData.isMissingNode() || structuredData.isNull()) {
            log.warn("Webhook end-of-call-report sin analysis.structuredData para callId={}", callId);
            return;
        }

        String externalUserId = textValue(structuredData.path("userId"));
        Long usuarioId = parseNumericUserId(externalUserId);
        if (usuarioId == null) {
            log.warn("No se pudo extraer userId numérico desde userId={} para callId={}", externalUserId, callId);
            return;
        }

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(usuarioId);
        if (usuarioOptional.isEmpty()) {
            log.warn("Usuario no encontrado para userId={} (callId={})", usuarioId, callId);
            return;
        }

        Lead lead = new Lead();
        lead.setCallId(callId);
        lead.setUsuario(usuarioOptional.get());
        lead.setModo(defaultMode(textValue(structuredData.path("modo"))));
        lead.setNombreCliente(textValue(structuredData.path("nombre_cliente")));
        lead.setTelefono(textValue(structuredData.path("telefono")));
        lead.setEmail(textValue(structuredData.path("email")));
        lead.setPresupuesto(textValue(structuredData.path("presupuesto")));
        lead.setZonaInteres(textValue(structuredData.path("zona_interes")));
        lead.setTipoPropiedad(textValue(structuredData.path("tipo_propiedad")));
        lead.setContexto(textValue(structuredData.path("contexto_resumido")));
        lead.setTiming(textValue(structuredData.path("timing")));
        lead.setGarantia(textValue(structuredData.path("garantia")));
        lead.setNivelLead(textValue(structuredData.path("nivel_lead")));
        lead.setProximoPaso(textValue(structuredData.path("proximo_paso")));
        lead.setObjecionesJson(toJsonOrNull(structuredData.path("objeciones")));

        leadRepository.save(lead);
    }

    private JsonNode unwrapMessageNode(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }
        JsonNode message = payload.path("message");
        if (!message.isMissingNode() && !message.isNull() && message.isObject()) {
            return message;
        }
        return payload;
    }

    private String defaultMode(String modo) {
        return (modo == null || modo.isBlank()) ? "desconocido" : modo;
    }

    private String textValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value != null ? value.trim() : null;
    }

    private Long parseNumericUserId(String rawUserId) {
        if (rawUserId == null || rawUserId.isBlank()) {
            return null;
        }
        Matcher matcher = USER_ID_PATTERN.matcher(rawUserId.trim());
        if (!matcher.matches()) {
            return null;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            log.error("Formato inválido de userId en webhook Vapi: {}", rawUserId, e);
            return null;
        }
    }

    private String toJsonOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("Error serializando objeciones de Vapi", e);
            return null;
        }
    }
}
