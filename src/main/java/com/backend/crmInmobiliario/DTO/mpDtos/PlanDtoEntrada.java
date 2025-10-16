package com.backend.crmInmobiliario.DTO.mpDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;
import java.math.BigDecimal;

public class PlanDtoEntrada {

    private String reason;

    // CORRECCIÓN: Usamos @JsonProperty para mapear 'back_url' (JSON) a 'backUrl' (Java DTO)
    @JsonProperty("back_url")
    private String backUrl;

    // Campo anidado
    @JsonProperty("auto_recurring") // Importante para snake_case en JSON
    private AutoRecurringDto autoRecurring;

    // Getters y Setters
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getBackUrl() {
        return backUrl;
    }
    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public AutoRecurringDto getAutoRecurring() {
        return autoRecurring;
    }
    public void setAutoRecurring(AutoRecurringDto autoRecurring) {
        this.autoRecurring = autoRecurring;
    }

    // Clase interna para manejar la estructura anidada 'auto_recurring'
    public static class AutoRecurringDto {
        private Integer frequency;

        @JsonProperty("frequency_type") // Asegurando el mapeo de snake_case
        private String frequencyType;

        @JsonDeserialize(using = BigDecimalDeserializer.class)
        @JsonProperty("transaction_amount") // Asegurando el mapeo de snake_case
        private BigDecimal transactionAmount;

        @JsonProperty("currency_id") // Asegurando el mapeo de snake_case
        private String currencyId;

        // --- Nuevos Campos Opcionales según la respuesta MP ---
        private Integer repetitions;

        @JsonProperty("free_trial")
        private FreeTrialDto freeTrial;

        @JsonProperty("billing_day")
        private Integer billingDay;

        @JsonProperty("billing_day_proportional")
        private Boolean billingDayProportional;
        // -----------------------------------------------------

        // Getters y Setters para AutoRecurringDto
        public Integer getFrequency() {
            return frequency;
        }
        public void setFrequency(Integer frequency) {
            this.frequency = frequency;
        }

        public String getFrequencyType() {
            return frequencyType;
        }
        public void setFrequencyType(String frequencyType) {
            this.frequencyType = frequencyType;
        }

        public BigDecimal getTransactionAmount() {
            return transactionAmount;
        }
        public void setTransactionAmount(BigDecimal transactionAmount) {
            this.transactionAmount = transactionAmount;
        }

        public String getCurrencyId() {
            return currencyId;
        }
        public void setCurrencyId(String currencyId) {
            this.currencyId = currencyId;
        }

        // Getters y Setters para Nuevos Campos Opcionales
        public Integer getRepetitions() {
            return repetitions;
        }
        public void setRepetitions(Integer repetitions) {
            this.repetitions = repetitions;
        }

        public FreeTrialDto getFreeTrial() {
            return freeTrial;
        }
        public void setFreeTrial(FreeTrialDto freeTrial) {
            this.freeTrial = freeTrial;
        }

        public Integer getBillingDay() {
            return billingDay;
        }
        public void setBillingDay(Integer billingDay) {
            this.billingDay = billingDay;
        }

        public Boolean getBillingDayProportional() {
            return billingDayProportional;
        }
        public void setBillingDayProportional(Boolean billingDayProportional) {
            this.billingDayProportional = billingDayProportional;
        }
    }

    // Clase interna para manejar la estructura anidada 'free_trial'
    public static class FreeTrialDto {
        private Integer frequency;

        @JsonProperty("frequency_type")
        private String frequencyType;

        @JsonProperty("first_invoice_offset")
        private Integer firstInvoiceOffset;

        // Getters y Setters
        public Integer getFrequency() {
            return frequency;
        }
        public void setFrequency(Integer frequency) {
            this.frequency = frequency;
        }

        public String getFrequencyType() {
            return frequencyType;
        }
        public void setFrequencyType(String frequencyType) {
            this.frequencyType = frequencyType;
        }

        public Integer getFirstInvoiceOffset() {
            return firstInvoiceOffset;
        }
        public void setFirstInvoiceOffset(Integer firstInvoiceOffset) {
            this.firstInvoiceOffset = firstInvoiceOffset;
        }
    }
}
