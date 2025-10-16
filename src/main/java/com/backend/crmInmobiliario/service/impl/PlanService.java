package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.DTO.mpDtos.PlanDtoEntrada;
import com.backend.crmInmobiliario.DTO.mpDtos.PlanDtoSalida;
import com.backend.crmInmobiliario.entity.planesYSuscripciones.Plan;
import com.backend.crmInmobiliario.repository.pagosYSuscripciones.PlanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PlanService {

    // Nota: Es mejor obtener este valor de application.properties o de una variable de entorno.
    // Usar @Value("${mp.access.token}") es la práctica recomendada.
    @Value("APP_USR-5046281913019324-092713-840cbca15befb8043072e120e527ae92-157704548")
    private String mpAccessToken;

    private final RestTemplate restTemplate;
    private final PlanRepository planRepository;

    private static final String MP_PLAN_URL = "https://api.mercadopago.com/preapproval_plan";

    public PlanService(RestTemplate restTemplate, PlanRepository planRepository) {
        this.restTemplate = restTemplate;
        this.planRepository = planRepository;
    }

    /**
     * Crea un plan en Mercado Pago y guarda el ID externo y los detalles de recurrencia en la BD local.
     * @param dto El DTO con la configuración del plan (motivo, precio, frecuencia, etc.).
     * @param planCode Código interno del plan (e.g., "PROFESIONAL").
     * @param planName Nombre del plan.
     * @param contractLimit Límite de contratos del plan.
     * @return PlanDtoSalida con el ID del plan creado en MP.
     */
    @Transactional
    public PlanDtoSalida createPlan(PlanDtoEntrada dto, String planCode, String planName, Integer contractLimit) {

        // 1. Validación de campos obligatorios
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'reason' es obligatorio.");
        }
        if (dto.getBackUrl() == null || dto.getBackUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'backUrl' es obligatorio.");
        }
        if (dto.getAutoRecurring() == null) {
            throw new IllegalArgumentException("El objeto 'autoRecurring' es obligatorio.");
        }

        // Validación de campos dentro de AutoRecurring
        if (dto.getAutoRecurring().getTransactionAmount() == null) {
            throw new IllegalArgumentException("El campo 'transactionAmount' (monto) es obligatorio dentro de autoRecurring.");
        }
        if (dto.getAutoRecurring().getCurrencyId() == null || dto.getAutoRecurring().getCurrencyId().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'currencyId' (moneda) es obligatorio dentro de autoRecurring.");
        }
        if (dto.getAutoRecurring().getFrequency() == null || dto.getAutoRecurring().getFrequency() <= 0) {
            throw new IllegalArgumentException("El campo 'frequency' es obligatorio y debe ser mayor que cero.");
        }
        if (dto.getAutoRecurring().getFrequencyType() == null || dto.getAutoRecurring().getFrequencyType().trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'frequencyType' es obligatorio.");
        }

        // 2. Crear el cuerpo de la solicitud en el formato que espera MP
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", dto.getReason());
        requestBody.put("back_url", dto.getBackUrl());

        // Configuración de recurrencia (AutoRecurringDto)
        Map<String, Object> autoRecurring = new HashMap<>();
        autoRecurring.put("frequency", dto.getAutoRecurring().getFrequency());
        autoRecurring.put("frequency_type", dto.getAutoRecurring().getFrequencyType());

        // Es crucial convertir BigDecimal a double/float para la serialización JSON.
        autoRecurring.put("transaction_amount", dto.getAutoRecurring().getTransactionAmount().doubleValue());
        autoRecurring.put("currency_id", dto.getAutoRecurring().getCurrencyId());

        // --- Mapeo de campos de facturación avanzados (NUEVO) ---
        if (dto.getAutoRecurring().getRepetitions() != null) {
            autoRecurring.put("repetitions", dto.getAutoRecurring().getRepetitions());
        }
        if (dto.getAutoRecurring().getBillingDay() != null) {
            autoRecurring.put("billing_day", dto.getAutoRecurring().getBillingDay());
        }
        if (dto.getAutoRecurring().getBillingDayProportional() != null) {
            autoRecurring.put("billing_day_proportional", dto.getAutoRecurring().getBillingDayProportional());
        }

        // Mapeo de Free Trial si está presente
        if (dto.getAutoRecurring().getFreeTrial() != null) {
            Map<String, Object> freeTrial = new HashMap<>();
            freeTrial.put("frequency", dto.getAutoRecurring().getFreeTrial().getFrequency());
            freeTrial.put("frequency_type", dto.getAutoRecurring().getFreeTrial().getFrequencyType());

            // Asumiendo que firstInvoiceOffset es opcional en tu DTO (si existe)
            if (dto.getAutoRecurring().getFreeTrial().getFirstInvoiceOffset() != null) {
                freeTrial.put("first_invoice_offset", dto.getAutoRecurring().getFreeTrial().getFirstInvoiceOffset());
            }
            autoRecurring.put("free_trial", freeTrial);
        }
        // ------------------------------------------------------------

        requestBody.put("auto_recurring", autoRecurring);

        // También enviamos payment_methods_allowed si está presente en el DTO (debe tener las clases correspondientes)
        // CORRECCIÓN: Se accede a 'dto' directamente, no a través de 'dto.getAutoRecurring()'

        // 3. Configurar headers (incluyendo el Access Token de autorización)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mpAccessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 4. Llamar a la API de Mercado Pago
        ResponseEntity<PlanDtoSalida> response;
        try {
            response = restTemplate.exchange(
                    MP_PLAN_URL,
                    HttpMethod.POST,
                    entity,
                    PlanDtoSalida.class
            );
        } catch (Exception e) {
            // Manejo de errores de la API (ej. 400 Bad Request, 500 Internal Error)
            System.err.println("Error al crear plan en Mercado Pago: " + e.getMessage());
            throw new RuntimeException("Error al crear el plan en MP.", e);
        }

        PlanDtoSalida mpResponse = response.getBody();

        if (mpResponse == null || mpResponse.getId() == null) {
            throw new RuntimeException("Respuesta inválida de Mercado Pago al crear el plan.");
        }

        // 5. Guardar en la base de datos local
        Plan newPlan = new Plan();
        newPlan.setCode(planCode);
        newPlan.setName(planName);
        newPlan.setContractLimit(contractLimit);
        newPlan.setActive(true);
        newPlan.setExternalPlanId(mpResponse.getId()); // ID de preapproval_plan

        // --- Mapeo de campos de recurrencia a la Entidad Plan ---
        newPlan.setPriceArs(dto.getAutoRecurring().getTransactionAmount());
        newPlan.setFrequency(dto.getAutoRecurring().getFrequency());
        newPlan.setFrequencyType(dto.getAutoRecurring().getFrequencyType());
        newPlan.setCurrencyId(dto.getAutoRecurring().getCurrencyId());
        // --------------------------------------------------------------------------

        planRepository.save(newPlan);

        return mpResponse;
    }
}
