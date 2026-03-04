package com.backend.crmInmobiliario.service.impl.n8n;

import com.backend.crmInmobiliario.DTO.entrada.N8N.AysaWebhookRequest;
import com.backend.crmInmobiliario.DTO.salida.N8N.AysaScrapeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class N8nAysaService {

    private final WebClient webClient;

    @Value("${n8n.aysa.webhook.url}")
    private String n8nUrl;

    public AysaScrapeResponse llamarWebhookAysa(AysaWebhookRequest req, String bearerToken) {

        WebClient.RequestBodySpec request = webClient.post()
                .uri(n8nUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON);

        if (bearerToken != null && !bearerToken.isBlank()) {
            request = request.header(HttpHeaders.AUTHORIZATION, bearerToken);
        }

        return request
                .bodyValue(req)
                .retrieve()
                .bodyToMono(AysaScrapeResponse.class)
                .block();
    }
}
