package com.backend.crmInmobiliario.service.impl.vapi;

import com.backend.crmInmobiliario.DTO.entrada.vapi.VapiOutboundCallRequest;
import com.backend.crmInmobiliario.config.properties.VapiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VapiCallService {

    private final RestTemplate restTemplate;
    private final VapiProperties vapiProperties;

    public JsonNode startOutboundCall(Long usuarioId, VapiOutboundCallRequest request) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuario autenticado inválido");
        }
        if (request.getModo() == null || request.getModo().isBlank()) {
            throw new IllegalArgumentException("modo es obligatorio");
        }
        if (request.getContexto() == null || request.getContexto().isBlank()) {
            throw new IllegalArgumentException("contexto es obligatorio");
        }

        String url = vapiProperties.getBaseUrl() + "/calls";

        Map<String, Object> payload = Map.of(
                "assistantId", vapiProperties.getAssistantId(),
                "phoneNumberId", vapiProperties.getPhoneNumberId(),
                "customer", Map.of("number", request.getDestinationNumber()),
                "assistantOverrides", Map.of(
                        "variableValues", Map.of(
                                "userId", "user_" + usuarioId,
                                "modo", request.getModo(),
                                "contexto", request.getContexto()
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(vapiProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
        return response.getBody();
    }
}
