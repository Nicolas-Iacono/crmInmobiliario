//package com.backend.crmInmobiliario.service.impl;
//
//import jakarta.transaction.Transactional;
//import org.springframework.stereotype.Service;
//
//@Service
//public class SubscriptionService {
//
//    @Transactional
//    public CheckoutResponse initCheckout(Long usuarioId, String planCode) {
//        // 1) validar plan activo
//        // 2) obtener/crear externalCustomerId
//        // 3) crear preferencia (MP) / session (Stripe) -> redirectUrl
//        // 4) guardar "pending intent" si querés
//        return new CheckoutResponse(redirectUrl);
//    }
//
//    @Transactional
//    public void activateFromWebhook(ProviderEvent e) {
//        // mapear externalSubscriptionId -> usuario
//        // poner plan correcto, status ACTIVE/TRIALING, currentPeriodEnd
//    }
//
//    @Transactional
//    public void markPastDue(Long usuarioId) {
//        // status -> PAST_DUE (seguir dando acceso durante gracia si querés)
//    }
//
//    @Transactional
//    public void cancelNow(Long usuarioId) {
//        // status -> CANCELED y mover a FREE automáticamente
//        // o dejar sin plan con contractLimit=0 (recomiendo bajar a FREE)
//        moveToFreeIfNeeded(usuarioId);
//    }
//
//    @Transactional
//    public void changePlan(Long usuarioId, String newPlanCode) {
//        // valida downgrade safe (ver sección 5)
//        // si ok: actualizar plan + fechas (mantener currentPeriodEnd)
//    }
//
//    public PlanLimitsDto getLimits(Long usuarioId) {
//        // leer plan actual, devolver {contractLimit, status, trialEndsAt, currentPeriodEnd}
//    }
//}
