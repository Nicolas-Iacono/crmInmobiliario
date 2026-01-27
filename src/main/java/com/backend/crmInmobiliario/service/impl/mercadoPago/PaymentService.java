// ✅ PaymentService.java
package com.backend.crmInmobiliario.service.impl.mercadoPago;


import com.backend.crmInmobiliario.DTO.mpDtos.SubscriptionRequest;
import com.backend.crmInmobiliario.DTO.mpDtos.SubscriptionResponse;
import com.backend.crmInmobiliario.DTO.salida.planesYSuscripcion.PaymentSalidaDto;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Payment;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Subscription;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PaymentRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(RestTemplate restTemplate, UsuarioRepository usuarioRepository, PlanRepository planRepository, SubscriptionRepository subscriptionRepository, PaymentRepository paymentRepository) {
        this.restTemplate = restTemplate;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
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

    public void cancelSubscription(String preapprovalId) {
        String url = "https://api.mercadopago.com/preapproval/" + preapprovalId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mpAccessToken);

        Map<String, Object> body = Map.of("status", "cancelled");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
            System.out.println("✅ Suscripción cancelada en Mercado Pago: " + preapprovalId);
        } catch (Exception e) {
            System.err.println("❌ Error al cancelar la suscripción en MP: " + e.getMessage());
            throw new RuntimeException("Error al cancelar la suscripción en Mercado Pago", e);
        }
    }


    @Transactional
    public void registrarPago(Map<String, Object> paymentData) {
        try {
            String paymentId = String.valueOf(paymentData.get("id"));
            String preapprovalId = String.valueOf(paymentData.get("preapproval_id"));
            String status = String.valueOf(paymentData.get("status"));
            String currency = String.valueOf(paymentData.get("currency_id"));

            BigDecimal amount = new BigDecimal(String.valueOf(paymentData.get("transaction_amount")));

            Map<String, Object> payer = (Map<String, Object>) paymentData.get("payer");
            String userIdStr = payer != null ? String.valueOf(payer.get("id")) : null;

            // Buscar usuario y suscripción asociada
            Subscription sub = subscriptionRepository.findByExternalSubscriptionId(preapprovalId)
                    .orElse(null);

            Usuario usuario = null;
            if (sub != null) {
                usuario = sub.getUsuario();
            } else if (userIdStr != null) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    usuario = usuarioRepository.findById(userId).orElse(null);
                } catch (NumberFormatException ignored) {}
            }

            if (usuario == null) {
                System.err.println("⚠️ No se encontró usuario asociado al pago " + paymentId);
                return;
            }

            // Verificar duplicados
            if (paymentRepository.findByMpPaymentId(paymentId).isPresent()) {
                System.out.println("ℹ️ El pago " + paymentId + " ya está registrado, se omite.");
                return;
            }

            // Crear el registro
            Payment pago = Payment.builder()
                    .mpPaymentId(paymentId)
                    .preapprovalId(preapprovalId)
                    .usuario(usuario)
                    .subscription(sub)
                    .status(status)
                    .amount(amount)
                    .currency(currency)
                    .paymentDate(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            paymentRepository.save(pago);
            System.out.printf("✅ Pago registrado: user=%s | amount=%s %s | status=%s%n",
                    usuario.getUsername(), amount, currency, status);

        } catch (Exception e) {
            System.err.println("❌ Error registrando pago: " + e.getMessage());
        }
    }

    public List<PaymentSalidaDto> obtenerPagosPorUsuario(String username) {
        List<Payment> pagos = paymentRepository.findByUsuarioUsernameOrderByPaymentDateDesc(username);

        return pagos.stream().map(p -> PaymentSalidaDto.builder()
                .id(p.getId())
                .planName(p.getSubscription() != null
                        ? p.getSubscription().getPlan().getName()
                        : "Desconocido")
                .mpPaymentId(p.getMpPaymentId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .paymentDate(p.getPaymentDate())
                .build()
        ).collect(Collectors.toList());
    }

}
