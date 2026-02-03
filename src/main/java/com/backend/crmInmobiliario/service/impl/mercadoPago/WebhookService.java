package com.backend.crmInmobiliario.service.impl.mercadoPago;

import com.backend.crmInmobiliario.entity.Recibo;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Subscription;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ReciboRepository;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
@Slf4j
@Service
public class WebhookService {
    @Value("${mp.access.token}")
    private String mpAccessToken;

    @Value("${mp.service.url:https://mpserviceapp-production.up.railway.app/}")
    private String mpServiceUrl;

    private final RestTemplate restTemplate;
    private final SubscriptionRepository subscriptionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;
    private final ReciboRepository reciboRepository;
    private PaymentService paymentService;
    public WebhookService(ReciboRepository reciboRepository, PaymentService paymentService, RestTemplate restTemplate, SubscriptionRepository subscriptionRepository,
                          UsuarioRepository usuarioRepository, PlanRepository planRepository) {
        this.restTemplate = restTemplate;
        this.subscriptionRepository = subscriptionRepository;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
        this.paymentService = paymentService;
        this.reciboRepository = reciboRepository;
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

        System.out.println("🚀 Procesando Webhook:");
        System.out.println(" - Type recibido: " + type);
        System.out.println(" - Data ID: " + dataId);

        // Normalizamos el tipo para manejar las variaciones de MP
        String normalizedType = type.toLowerCase();

        // Si es una notificación de Suscripción (Preapproval) o Pago
        if (normalizedType.contains("preapproval")) {
            System.out.println("🟢 Tipo detectado: preapproval → ejecutando processPreapproval");
            // Esto cubre: "preapproval" y "subscription_preapproval"
            processPreapproval(dataId);
        } else if (normalizedType.equals("payment")) {
            System.out.println("🟣 Tipo detectado: payment → ejecutando processPayment");
            // Cubre el caso de notificación de pago
            processPayment(dataId);
        }
        else if (normalizedType.equals("subscription_authorized_payment")) {
                System.out.println("💰 Tipo detectado: subscription_authorized_payment → ejecutando processAuthorizedPayment");
                processAuthorizedPayment(dataId);
        } else {
            System.out.println("Webhook - Tipo de notificación de MP no manejado: " + type);
        }
    }

    /**
     * Procesa un evento de pago (payment).
     * Si el pago está asociado a una preaprobación, llama a processPreapproval.
     * @param paymentId ID del pago
     */
//    private void processPayment(String paymentId) {
//        try {
//            // 1. Consultar el detalle del pago a través del microservicio de Node.js
//            String url = mpServiceUrl + "/api/mp/payments/" + paymentId;
//            Map<String, Object> mpPayment = restTemplate.getForObject(url, Map.class);
//            if (mpPayment == null) return;
//
//            // 2. Verificar si este pago pertenece a una suscripción (preapproval_id)
//            // MP usa 'external_reference' si tú lo configuraste en la creación del pago
//            String preapprovalId = (String) mpPayment.get("external_reference");
//
//            // Si el pago no usa external_reference, buscamos en metadata (MP puede meterlo ahí)
//            if (preapprovalId == null || preapprovalId.isEmpty()) {
//                Map<String, Object> metadata = (Map<String, Object>) mpPayment.get("metadata");
//                if (metadata != null && metadata.containsKey("preapproval_id")) {
//                    preapprovalId = String.valueOf(metadata.get("preapproval_id"));
//                }
//            }
//
//            if (preapprovalId != null && !preapprovalId.isEmpty()) {
//                System.out.println("Pago (" + paymentId + ") asociado a Preapproval (" + preapprovalId + "). Procesando como Preapproval...");
//                processPreapproval(preapprovalId);
//            } else {
//                // Esto podría ser un pago único, no relacionado con suscripciones
//                System.out.println("Pago (" + paymentId + ") recibido. No asociado a ninguna suscripción (external_reference/metadata faltante).");
//            }
//
//        } catch (Exception e) {
//            System.err.println("Error al procesar el pago Webhook MP " + paymentId + ": " + e.getMessage());
//        }
//    }

//    private void processPayment(String paymentId) {
//        try {
//            // 1️⃣ Consultar el detalle del pago a través del microservicio Node.js
//            String url = mpServiceUrl + "/api/mp/payments/" + paymentId;
//            Map<String, Object> mpPayment = restTemplate.getForObject(url, Map.class);
//            if (mpPayment == null) return;
//
//            System.out.println("📦 Datos de pago recibidos: " + mpPayment);
//
//            // 2️⃣ Intentar obtener el preapproval_id real
//            String preapprovalId = (String) mpPayment.get("preapproval_id"); // ✅ este es el correcto
//
//            // 3️⃣ Fallbacks si no vino explícitamente
//            if (preapprovalId == null || preapprovalId.isEmpty()) {
//                // A veces Mercado Pago lo manda en metadata
//                Map<String, Object> metadata = (Map<String, Object>) mpPayment.get("metadata");
//                if (metadata != null && metadata.containsKey("preapproval_id")) {
//                    preapprovalId = String.valueOf(metadata.get("preapproval_id"));
//                }
//            }
//
//            // 4️⃣ Si sigue sin aparecer, NO usar external_reference (porque es el userId)
//            if (preapprovalId == null || preapprovalId.isEmpty()) {
//                System.out.println("⚠️ Pago (" + paymentId + ") sin preapproval_id, no se procesará como suscripción.");
//                return;
//            }
//
//            // 5️⃣ Procesar correctamente la suscripción
//            System.out.println("💳 Pago (" + paymentId + ") asociado a Preapproval (" + preapprovalId + "). Procesando...");
//            processPreapproval(preapprovalId);
//
//        } catch (Exception e) {
//            System.err.println("❌ Error al procesar el pago Webhook MP " + paymentId + ": " + e.getMessage());
//        }
//    }
    private void processPayment(String paymentId) {
        try {
            String url = mpServiceUrl + "/api/mp/payments/" + paymentId;
            Map<String, Object> mpPayment = restTemplate.getForObject(url, Map.class);
            if (mpPayment == null) return;

            log.info("📦 Datos de pago recibidos: {}", mpPayment);

            // ✅ 1) Si viene preapproval_id → es pago de suscripción
            String preapprovalId = (String) mpPayment.get("preapproval_id");
            if (preapprovalId == null || preapprovalId.isEmpty()) {
                Map<String, Object> metadata = (Map<String, Object>) mpPayment.get("metadata");
                if (metadata != null && metadata.get("preapproval_id") != null) {
                    preapprovalId = String.valueOf(metadata.get("preapproval_id"));
                }
            }

            if (preapprovalId != null && !preapprovalId.isEmpty()) {
                log.info("💳 payment {} asociado a preapproval {} → suscripción", paymentId, preapprovalId);
                processPreapproval(preapprovalId);
                return;
            }

            // ✅ 2) Si NO viene preapproval_id → tratá como pago de recibo (pago puntual)
            log.info("🧾 payment {} sin preapproval_id → lo tomo como pago de recibo", paymentId);
            processReciboPayment(mpPayment, paymentId);

        } catch (Exception e) {
            log.error("❌ Error al procesar payment {}: {}", paymentId, e.getMessage());
        }
    }

    @Transactional
    private void processReciboPayment(Map<String, Object> mpPayment, String paymentId) {

        String status = (String) mpPayment.get("status"); // approved / pending / rejected
        String externalReference = (String) mpPayment.get("external_reference");

        Object amountObj = mpPayment.get("transaction_amount");
        BigDecimal amount = amountObj != null ? new BigDecimal(amountObj.toString()) : BigDecimal.ZERO;

        if (externalReference == null || externalReference.isBlank()) {
            log.warn("⚠️ Pago {} sin external_reference → no puedo asociarlo a un recibo", paymentId);
            return;
        }

        // Si usás external_reference para usuario en suscripciones, para recibos hacé que empiece con "RECIBO-"
        if (!externalReference.startsWith("RECIBO-")) {
            log.info("ℹ️ Pago {} external_reference={} no parece de recibo → lo ignoro", paymentId, externalReference);
            return;
        }

        Recibo recibo = reciboRepository.findByMpExternalReference(externalReference)
                .orElse(null);

        if (recibo == null) {
            log.warn("⚠️ No existe recibo con mpExternalReference={}", externalReference);
            return;
        }

        // Idempotencia
        if (Boolean.TRUE.equals(recibo.getEstado())) {
            log.info("✅ Recibo {} ya estaba pago. Ignoro duplicado.", recibo.getId());
            return;
        }

        // Validación de monto mínimo
        BigDecimal esperado = recibo.getMontoTotal() != null ? recibo.getMontoTotal() : BigDecimal.ZERO;
        if (amount.compareTo(esperado) < 0) {
            log.warn("⚠️ Pago {} monto {} menor que esperado {} para recibo {}", paymentId, amount, esperado, recibo.getId());
            recibo.setMpPaymentId(paymentId);
            recibo.setMpStatus(status);
            reciboRepository.save(recibo);
            return;
        }

        // Guardar info MP
        recibo.setMpPaymentId(paymentId);
        recibo.setMpStatus(status);

        if ("approved".equalsIgnoreCase(status)) {
            recibo.setEstado(Boolean.TRUE);
            recibo.setMpPaidAt(LocalDateTime.now(ZoneOffset.UTC));
            log.info("🎉 Recibo {} marcado como PAGADO por payment {}", recibo.getId(), paymentId);
        } else {
            log.info("🟡 Recibo {} pago status={} (no se marca como pagado)", recibo.getId(), status);
        }

        reciboRepository.save(recibo);
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

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("📦 Datos recibidos de MP (" + preapprovalId + "):");
            mp.forEach((k, v) -> System.out.println(" - " + k + ": " + v));

            // 2️⃣ Extraer datos relevantes
            externalReference = (String) mp.get("external_reference");
            String mpStatus = (String) mp.get("status");
            String nextPaymentDateStr = (String) mp.get("next_payment_date");
            String externalPlanId = (String) mp.get("preapproval_plan_id");
            String reason = (String) mp.get("reason");
            String planCodeDetected = null;
            if (reason != null) {
                if (reason.contains("PLAN-")) {
                    planCodeDetected = reason.substring(reason.indexOf("PLAN-")).trim().split(" ")[0].trim().toUpperCase();
                } else if (reason.contains("-")) {
                    planCodeDetected = reason.substring(reason.lastIndexOf('-') + 1).trim().toUpperCase();
                }
            }
            System.out.println("🧠 Plan detectado preliminarmente desde reason: " + planCodeDetected);
            // 🧠 LOG EXTRA PARA VER CAMPOS CLAVE
            System.out.println("🧠 externalReference=" + externalReference
                    + " | mpStatus=" + mpStatus
                    + " | nextPaymentDate=" + nextPaymentDateStr
                    + " | externalPlanId=" + externalPlanId
                    + " | reason=" + reason);
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

            Subscription.Status finalStatus = mapStatus(mpStatus);

            Optional<Subscription> subOpt = subscriptionRepository.findByExternalSubscriptionId(preapprovalId);

// 🧩 Si no se encuentra por preapproval_id, buscar por usuario_id (por external_reference)
            if (subOpt.isEmpty() && externalReference != null) {
                try {
                    Long userId = Long.parseLong(externalReference);
                    subOpt = subscriptionRepository.findByUsuarioId(userId);
                    if (subOpt.isPresent()) {
                        System.out.println("🔁 Suscripción existente encontrada por usuario_id=" + userId);
                    }
                } catch (NumberFormatException ignored) {}
            }



            Subscription sub;
            Usuario usuario;
            Plan plan;

            // 🔹 Si no se encuentra por externalSubscriptionId, buscar por usuario_id (external_reference)
            if (subOpt.isPresent()) {
                sub = subOpt.get();
                usuario = sub.getUsuario();

                // 🧩 Detectar plan desde reason (por ejemplo "Tuinmo - PLAN-BARATO")
                plan = sub.getPlan(); // valor por defecto

                if (reason != null && reason.contains("PLAN-")) {
                    String planCode = reason.substring(reason.indexOf("PLAN-")).trim();
                    planCode = planCode.split(" ")[0].trim().toUpperCase();

                    System.out.println("🧩 Plan detectado en reason: " + planCode);

                    String finalPlanCode = planCode;
                    plan = planRepository.findByCode(planCode)
                            .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado por código: " + finalPlanCode));

                    sub.setPlan(plan); // actualizamos el plan de la suscripción
                    System.out.println("🔁 Plan actualizado en la suscripción: " + plan.getCode());
                }

                sub.setExternalSubscriptionId(preapprovalId);
                sub.setStatus(finalStatus);
                sub.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

                if (finalStatus == Subscription.Status.ACTIVE
                        || finalStatus == Subscription.Status.AUTHORIZED
                        || finalStatus == Subscription.Status.TRIALING) {

                    sub.setTrialEndsAt(null);
                    if (nextPaymentDateStr != null) {
                        Instant currentPeriodEnd = Instant.parse(nextPaymentDateStr);
                        sub.setCurrentPeriodEnd(LocalDateTime.ofInstant(currentPeriodEnd, ZoneOffset.UTC));
                    } else if (sub.getCurrentPeriodEnd() == null) {
                        sub.setCurrentPeriodEnd(LocalDateTime.now(ZoneOffset.UTC).plusDays(30));
                    }

                    // 🔸 Actualizar plan también en el usuario
                    usuario.setPlan(plan);
                    usuarioRepository.save(usuario);
                    System.out.printf("🎉 Plan '%s' asignado al usuario '%s'%n",
                            plan.getCode(), usuario.getUsername());
                } else if (finalStatus == Subscription.Status.CANCELED) {
                    sub.setCurrentPeriodEnd(null);
                    sub.setCancelAtPeriodEnd(false); // 🔸 Revertir al plan FREE
                    Plan planFree = planRepository.findByCode("FREE")
                            .orElseThrow(() -> new IllegalStateException("Plan FREE no encontrado"));
                    sub.setPlan(planFree);

                    usuario = sub.getUsuario();
                    if (usuario != null) {
                        usuario.setPlan(planFree);
                        usuarioRepository.save(usuario);
                        System.out.printf("🚫 Suscripción cancelada en MP → usuario '%s' pasado al plan FREE%n",
                                usuario.getUsername());
                    }
                }

                subscriptionRepository.save(sub);
                System.out.println("🔄 Suscripción actualizada para usuario " + usuario.getId() + " | status=" + sub.getStatus());
                return;
            }

            // 🟢 Si no existía, crear una nueva
            if (externalReference == null)
                throw new IllegalArgumentException("External Reference (User ID) es nulo en la respuesta de MP.");

            Long userId = Long.parseLong(externalReference);
            usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado (ID: " + userId + ")"));

            // 🔹 Buscar el plan (bloque robusto con soporte para todos los casos)
            try {
                if (externalPlanId != null && !externalPlanId.isEmpty()) {
                    System.out.println("🔍 Buscando plan por externalPlanId: " + externalPlanId);
                    plan = planRepository.findByExternalPlanId(externalPlanId)
                            .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado por externalPlanId: " + externalPlanId));

                } else if (reason != null) {
                    String planCode = null;

                    // Caso 1: el reason contiene "PLAN-" → extraer desde ahí
                    if (reason.contains("PLAN-")) {
                        planCode = reason.substring(reason.indexOf("PLAN-")).trim();
                    }
                    // Caso 2: no tiene "PLAN-", pero sí un nombre al final
                    else if (reason.contains("-")) {
                        planCode = reason.substring(reason.lastIndexOf('-') + 1).trim().toUpperCase();
                    }

                    if (planCode != null && !planCode.isEmpty()) {
                        System.out.println("🔍 Buscando plan por código: " + planCode);
                        final String finalPlanCode = planCode;
                        plan = planRepository.findByCode(finalPlanCode)
                                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado por código: " + finalPlanCode));
                        System.out.println("✅ Plan encontrado: " + plan.getName());
                    } else {
                        throw new ResourceNotFoundException("No se pudo extraer el código del plan desde reason: " + reason);
                    }

                } else if (mp.containsKey("auto_recurring")) {
                    // Fallback: deducir plan por monto del pago
                    Map<String, Object> autoRecurring = (Map<String, Object>) mp.get("auto_recurring");
                    Object amountObj = autoRecurring != null ? autoRecurring.get("transaction_amount") : null;

                    if (amountObj != null) {
                        double amount = Double.parseDouble(amountObj.toString());
                        System.out.println("💡 Intentando deducir plan por monto: " + amount);

                        plan = planRepository.findByPriceArs(BigDecimal.valueOf(amount))
                                .orElseThrow(() -> new ResourceNotFoundException("Plan no encontrado por monto: " + amount));
                        System.out.println("✅ Plan deducido por monto: " + plan.getName());
                    } else {
                        throw new ResourceNotFoundException("auto_recurring sin monto.");
                    }

                } else {
                    throw new ResourceNotFoundException("No se pudo deducir el plan (sin externalPlanId, reason ni auto_recurring).");
                }

            } catch (ResourceNotFoundException e) {
                System.err.println("⚠️ " + e.getMessage());
                System.out.println("➡️ Asignando plan FREE como fallback.");
                plan = planRepository.findByCode("FREE")
                        .orElseThrow(() -> new IllegalStateException("❌ Plan FREE no encontrado en la base de datos."));
            }

            // 4️⃣ Crear nueva suscripción
            sub = subOpt.orElseGet(Subscription::new);

            sub.setUsuario(usuario);
            sub.setPlan(plan);
            sub.setExternalCustomerId(usuario.getId().toString());
            sub.setExternalSubscriptionId(preapprovalId);
            sub.setCancelAtPeriodEnd(false);
            sub.setStatus(finalStatus);
            sub.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

            if (sub.getCreatedAt() == null)
                sub.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

            if (finalStatus == Subscription.Status.ACTIVE && nextPaymentDateStr != null) {
                Instant currentPeriodEnd = Instant.parse(nextPaymentDateStr);
                sub.setCurrentPeriodEnd(LocalDateTime.ofInstant(currentPeriodEnd, ZoneOffset.UTC));
            }

            subscriptionRepository.save(sub);
            System.out.printf("✅ Suscripción actualizada/creada: user=%d | plan=%s | status=%s%n",
                    usuario.getId(), plan.getCode(), finalStatus);

            // 🔸 Si la suscripción está activa, actualizar el plan del usuario
            if (finalStatus == Subscription.Status.ACTIVE) {
                try {
                    usuario.setPlan(plan);
                    usuarioRepository.save(usuario);
                    System.out.printf("🎉 Plan '%s' asignado al usuario '%s'%n",
                            plan.getCode(), usuario.getUsername());
                } catch (Exception e) {
                    System.err.printf("⚠️ Error al asignar el plan al usuario %d: %s%n",
                            usuario.getId(), e.getMessage());
                }
            }

        } catch (NumberFormatException e) {
            System.err.printf("❌ External_reference inválido (%s): %s%n", externalReference, e.getMessage());
        } catch (Exception e) {
            System.err.printf("⚠️ Error Webhook MP %s: could not execute statement [%s]%n",
                    preapprovalId, e.getMessage());
        }
    }


    /**
     * Procesa un evento de pago recurrente (subscription_authorized_payment).
     * Llama a la API de MP para obtener los detalles del pago y actualiza la suscripción.
     */
//    private void processAuthorizedPayment(String authorizedPaymentId) {
//        try {
//            // 1️⃣ Consultar los detalles del pago recurrente directamente desde Mercado Pago
//            String url = "https://api.mercadopago.com/authorized_payments/" + authorizedPaymentId;
//
//            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
//            headers.setBearerAuth(mpAccessToken); // ⚠️ reemplazá con tu token real de producción
//            headers.setAccept(java.util.Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));
//
//            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
//            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
//                    url,
//                    org.springframework.http.HttpMethod.GET,
//                    entity,
//                    Map.class
//            );
//
//            Map<String, Object> authorizedPayment = response.getBody();
//            if (authorizedPayment == null) {
//                System.err.println("⚠️ No se pudo obtener información del pago autorizado ID=" + authorizedPaymentId);
//                return;
//            }
//
//            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
//            System.out.println("📦 Detalle del pago recurrente recibido (authorized_payment):");
//            authorizedPayment.forEach((k, v) -> System.out.println(" - " + k + ": " + v));
//            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
//
//            // 2️⃣ Obtener el ID de la suscripción (preapproval_id)
//            String preapprovalId = (String) authorizedPayment.get("preapproval_id");
//            if (preapprovalId == null || preapprovalId.isEmpty()) {
//                System.err.println("⚠️ Pago recurrente sin preapproval_id → no se puede asociar a una suscripción.");
//                return;
//            }
//
//            // 3️⃣ Reutilizamos tu lógica existente
//            System.out.println("💳 Pago recurrente asociado a preapproval_id=" + preapprovalId);
//            processPreapproval(preapprovalId);
//            System.out.println("💳 Guardando registro de pago en base de datos...");
//            paymentService.registrarPago(authorizedPayment);
//        } catch (Exception e) {
//            System.err.println("❌ Error al procesar authorized_payment " + authorizedPaymentId + ": " + e.getMessage());
//        }
//    }
    private void processAuthorizedPayment(String authorizedPaymentId) {
        try {
            String url = "https://api.mercadopago.com/authorized_payments/" + authorizedPaymentId;

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(mpAccessToken);
            headers.setAccept(java.util.Collections.singletonList(org.springframework.http.MediaType.APPLICATION_JSON));

            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> authorizedPayment = response.getBody();
            if (authorizedPayment == null) {
                System.err.println("⚠️ No se pudo obtener información del pago autorizado ID=" + authorizedPaymentId);
                return;
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");
            System.out.println("📦 Detalle del pago recurrente recibido (authorized_payment):");
            authorizedPayment.forEach((k, v) -> System.out.println(" - " + k + ": " + v));
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------");

            // 2️⃣ Obtener el ID de la suscripción (preapproval_id)
            String preapprovalId = (String) authorizedPayment.get("preapproval_id");
            if (preapprovalId == null || preapprovalId.isEmpty()) {
                System.err.println("⚠️ Pago recurrente sin preapproval_id → no se puede asociar a una suscripción.");
                return;
            }

            // 3️⃣ Buscar la suscripción en la base
            Optional<Subscription> subOpt = subscriptionRepository.findByExternalSubscriptionId(preapprovalId);
            if (subOpt.isPresent()) {
                Subscription sub = subOpt.get();

                // 4️⃣ Actualizar la fecha de renovación actual
                Object dateApprovedObj = authorizedPayment.get("date_approved");
                if (dateApprovedObj != null) {
                    Instant dateApproved = Instant.parse(dateApprovedObj.toString());
                    LocalDateTime paymentDate = LocalDateTime.ofInstant(dateApproved, ZoneOffset.UTC);
                    LocalDateTime nextPeriodEnd = paymentDate.plusDays(30); // o según tu plan

                    sub.setCurrentPeriodEnd(nextPeriodEnd);
                    sub.setStatus(Subscription.Status.ACTIVE);
                    sub.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                    subscriptionRepository.save(sub);

                    System.out.printf("🔁 Suscripción renovada correctamente. Próximo vencimiento: %s%n", nextPeriodEnd);
                } else {
                    System.out.println("⚠️ No se encontró fecha de aprobación en el pago, no se actualizó currentPeriodEnd.");
                }

                // 5️⃣ Asegurar que el usuario sigue con el plan correcto
                Usuario usuario = sub.getUsuario();
                if (!usuario.getPlan().getId().equals(sub.getPlan().getId())) {
                    usuario.setPlan(sub.getPlan());
                    usuarioRepository.save(usuario);
                    System.out.printf("🎉 Plan '%s' confirmado para usuario '%s'%n",
                            sub.getPlan().getCode(), usuario.getUsername());
                }
            }

            // 6️⃣ Registrar el pago (ya lo hacías correctamente)
            System.out.println("💳 Guardando registro de pago en base de datos...");
            paymentService.registrarPago(authorizedPayment);

        } catch (Exception e) {
            System.err.println("❌ Error al procesar authorized_payment " + authorizedPaymentId + ": " + e.getMessage());
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
