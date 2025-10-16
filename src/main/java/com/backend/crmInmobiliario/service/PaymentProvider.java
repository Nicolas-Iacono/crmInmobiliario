package com.backend.crmInmobiliario.service;

import com.backend.crmInmobiliario.entity.Usuario;

public interface PaymentProvider {
    /** Crea/asegura un customer remoto y devuelve su id */
    String ensureCustomer(Usuario user);

    /** Crea una sesión/checkout de suscripción para un plan y devuelve la URL de redirección */
    String createCheckoutSession(String externalCustomerId, String planCode);

    /** Cambia el plan de una suscripción remota (si aplica) */
    void updateSubscriptionPlan(String externalSubscriptionId, String planCode);

    /** Cancela al final del periodo de facturación */
    void setCancelAtPeriodEnd(String externalSubscriptionId, boolean cancel);

    /** Cancela inmediata */
    void cancelSubscriptionNow(String externalSubscriptionId);
}
