package com.backend.crmInmobiliario.service.impl.mercadoPago;

import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Subscription;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

@Service
public class WebhookService {

    @Value("${mp.service.url:https://mpserviceapp-production.up.railway.app/}")
    private String mpServiceUrl;

    private final RestTemplate restTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;

    public WebhookService(RestTemplate restTemplate, SubscriptionRepository subscriptionRepository,
                          UsuarioRepository usuarioRepository, PlanRepository planRepository) {
        this.restTemplate = restTemplate;
        this.subscriptionRepository = subscriptionRepository;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
    }

    /**
     * Método que el Controller llama de forma asíncrona.
     * @param type Tipo de notificación (ej. preapproval, payment)
     * @param dataId ID de la entidad notificada
     */
    @Async // Ejecuta la lógica en un hilo separado
    public void handleNotificationAsync(String type, String dataId) {
        handleNotification(type, dataId);
    }

    // Método privado que contiene la lógica real
    private void handleNotification(String type, String dataId) {
        // Normalizamos el tipo para manejar las variaciones de MP
        String normalizedType = type.toLowerCase();

        // Si es una notificación de Suscripción (Preapproval) o Pago
        if (normalizedType.contains("preapproval")) {
            // Esto cubre: "preapproval" y "subscription_preapproval"
            processPreapproval(dataId);
        } else if (normalizedType.equals("payment")) {
            // Cubre el caso de notificación de pago
            processPayment(dataId);
        } else {
            System.out.println("Webhook - Tipo de notificación de MP no manejado: " + type);
        }
    }

    /**
     * Procesa un evento de pago (payment).
     * Si el pago está asociado a una preaprobación, llama a processPreapproval.
     * @param paymentId ID del pago
     */
    private void processPayment(String paymentId) {
        try {
            // 1. Consultar el detalle del pago a través del microservicio de Node.js
            String url = mpServiceUrl + "/api/mp/payments/" + paymentId;
            Map<String, Object> mpPayment = restTemplate.getForObject(url, Map.class);
            if (mpPayment == null) return;

            // 2. Verificar si este pago pertenece a una suscripción (preapproval_id)
            // MP usa 'external_reference' si tú lo configuraste en la creación del pago
            String preapprovalId = (String) mpPayment.get("external_reference");

            // Si el pago no usa external_reference, buscamos en metadata (MP puede meterlo ahí)
            if (preapprovalId == null || preapprovalId.isEmpty()) {
                Map<String, Object> metadata = (Map<String, Object>) mpPayment.get("metadata");
                if (metadata != null && metadata.containsKey("preapproval_id")) {
                    preapprovalId = String.valueOf(metadata.get("preapproval_id"));
                }
            }

            if (preapprovalId != null && !preapprovalId.isEmpty()) {
                System.out.println("Pago (" + paymentId + ") asociado a Preapproval (" + preapprovalId + "). Procesando como Preapproval...");
                processPreapproval(preapprovalId);
            } else {
                // Esto podría ser un pago único, no relacionado con suscripciones
                System.out.println("Pago (" + paymentId + ") recibido. No asociado a ninguna suscripción (external_reference/metadata faltante).");
            }

        } catch (Exception e) {
            System.err.println("Error al procesar el pago Webhook MP " + paymentId + ": " + e.getMessage());
        }
    }


    /**
     * REQUISITO 2B: Consulta la API de MP para obtener los detalles del evento
     * y actualiza la base de datos local.
     */
    @Transactional
    private void processPreapproval(String preapprovalId) {
        String externalReference = null;
        try {
            // 1️⃣ Obtener los datos desde tu microservicio Node.js
            String url = mpServiceUrl + "/api/mp/subscriptions/" + preapprovalId;
            Map<String, Object> mp = restTemplate.getForObject(url, Map.class);
            if (mp == null) {
                System.err.println("❌ No se pudo obtener datos desde el microservicio Node para preapprovalId=" + preapprovalId);
                return;
            }

            // 2️⃣ Extraer datos relevantes
            externalReference = (String) mp.get("external_reference");
            String mpStatus = (String) mp.get("status");
            String nextPaymentDateStr = (String) mp.get("next_payment_date");
            String externalPlanId = (String) mp.get("preapproval_plan_id");
            String reason = (String) mp.get("reason");

            Subscription.Status finalStatus = mapStatus(mpStatus);

            // 3️⃣ Buscar si ya existe por externalSubscriptionId
            Optional<Subscription> subOpt = subscriptionRepository.findByExternalSubscriptionId(preapprovalId);
            Subscription sub;
            Usuario usuario;
            Plan plan;

            // 🔹 Si no se encuentra por externalSubscriptionId, intentamos buscar por usuario_id
            if (subOpt.isEmpty() && externalReference != null) {
                try {
                    Long userId = Long.parseLong(externalReference);
                    subOpt = subscriptionRepository.findByUsuarioId(userId);
                } catch (NumberFormatException ignored) {}
            }

            if (subOpt.isPresent()) {
                // 🔁 Actualizar suscripción existente
                sub = subOpt.get();
                usuario = sub.getUsuario();
                plan = sub.getPlan();
                sub.setExternalSubscriptionId(preapprovalId);
                sub.setStatus(finalStatus);
                sub.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

                if (finalStatus == Subscription.Status.ACTIVE) {
                    sub.setTrialEndsAt(null);
                    if (nextPaymentDateStr != null) {
                        Instant currentPeriodEnd = Instant.parse(nextPaymentDateStr);
                        sub.setCurrentPeriodEnd(LocalDateTime.ofInstant(currentPeriodEnd, ZoneOffset.UTC));
                    } else if (sub.getCurrentPeriodEnd() == null) {
                        sub.setCurrentPeriodEnd(LocalDateTime.now(ZoneOffset.UTC).plusDays(30));
                    }
                } else if (finalStatus == Subscription.Status.CANCELED) {
                    sub.setCurrentPeriodEnd(null);
                    sub.setCancelAtPeriodEnd(false);
                }

                subscriptionRepository.save(sub);
                System.out.println("🔄 Suscripción actualizada para usuario " + usuario.getId() + " | status=" + sub.getStatus());
                return;
            }

            // 🟢 Si no existía, creamos una nueva
            if (externalReference == null)
                throw new IllegalArgumentException("External Reference (User ID) es nulo en la respuesta de MP.");

            Long userId = Long.parseLong(externalReference);
            usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado (ID: " + userId + ")"));

            // 🔹 Buscar el plan
            if (externalPlanId != null && !externalPlanId.isEmpty()) {
                plan = planRepository.findByExternalPlanId(externalPlanId)
                        .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado por externalPlanId: " + externalPlanId));
            } else if (reason != null && reason.contains("PLAN-")) {
                String planCode = reason.substring(reason.indexOf("PLAN-")).trim();
                plan = planRepository.findByCodeAndActiveTrue(planCode)
                        .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado por código: " + planCode));
            } else {
                throw new ResourceNotFoundException("No se pudo deducir el plan (sin externalPlanId ni reason válido).");
            }

            // 4️⃣ Crear nueva suscripción
            sub = Subscription.builder()
                    .usuario(usuario)
                    .plan(plan)
                    .externalCustomerId(usuario.getId().toString())
                    .externalSubscriptionId(preapprovalId)
                    .cancelAtPeriodEnd(false)
                    .status(finalStatus)
                    .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                    .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                    .build();

            if (finalStatus == Subscription.Status.ACTIVE && nextPaymentDateStr != null) {
                Instant currentPeriodEnd = Instant.parse(nextPaymentDateStr);
                sub.setCurrentPeriodEnd(LocalDateTime.ofInstant(currentPeriodEnd, ZoneOffset.UTC));
            }

            subscriptionRepository.save(sub);
            System.out.printf("✅ Nueva suscripción creada: user=%d | plan=%s | status=%s%n",
                    usuario.getId(), plan.getCode(), finalStatus);

        } catch (NumberFormatException e) {
            System.err.printf("❌ External_reference inválido (%s): %s%n", externalReference, e.getMessage());
        } catch (Exception e) {
            System.err.printf("⚠️ Error Webhook MP %s: could not execute statement [%s]%n",
                    preapprovalId, e.getMessage());
        }
    }



    private Subscription.Status mapStatus(String mpStatus) {
        return switch (mpStatus.toLowerCase()) {
            case "authorized" -> Subscription.Status.ACTIVE;
            case "pending",
                 "in_process" -> Subscription.Status.TRIALING;
            case "paused" -> Subscription.Status.PAST_DUE;   // suspensión por falta de pago
            case "cancelled" -> Subscription.Status.CANCELED;   // cancelación explícita por usuario
            case "finished" -> Subscription.Status.UNPAID;     // expiró por tiempo o límite
            default -> Subscription.Status.INCOMPLETE; // fallback
        };
    }
}
