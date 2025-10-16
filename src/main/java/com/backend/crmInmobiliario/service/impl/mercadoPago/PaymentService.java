// ✅ PaymentService.java
package com.backend.crmInmobiliario.service.impl.mercadoPago;


import com.backend.crmInmobiliario.DTO.mpDtos.SubscriptionRequest;
import com.backend.crmInmobiliario.DTO.mpDtos.SubscriptionResponse;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Value("${mp.access.token:}")
    private String mpAccessToken;

    @Value("${mp.notification.url:}")
    private String notificationUrl;

    @Value("${mp.service.url:https://mpserviceapp-production.up.railway.app}")
    private String mpServiceUrl;

    private final RestTemplate restTemplate;
    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;

    public PaymentService(RestTemplate restTemplate,
                          UsuarioRepository usuarioRepository,
                          PlanRepository planRepository) {
        this.restTemplate = restTemplate;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
    }

    public SubscriptionResponse initSubscriptionCheckout(Long usuarioId, String planCode) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        Plan plan = planRepository.findByCodeAndActiveTrue(planCode)
                .orElseThrow(() -> new EntityNotFoundException("Plan no encontrado con código: " + planCode));

        BigDecimal amount = plan.getPriceArs();

        SubscriptionRequest requestBody = new SubscriptionRequest(
                usuario.getEmail(),
                plan.getCode(),
                usuario.getId().toString(),
                amount,
                notificationUrl
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SubscriptionRequest> entity = new HttpEntity<>(requestBody, headers);

        String url = mpServiceUrl + "/api/mp/subscriptions/init";

        try {
            return restTemplate.postForObject(url, entity, SubscriptionResponse.class);
        } catch (Exception e) {
            System.err.println("Error al llamar al microservicio de MP: " + e.getMessage());
            throw new RuntimeException("Fallo en la comunicación con el servicio de Mercado Pago.", e);
        }
    }
}
