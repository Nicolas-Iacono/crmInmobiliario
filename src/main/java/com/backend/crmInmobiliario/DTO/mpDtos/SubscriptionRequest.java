// ✅ SubscriptionRequest.java
package com.backend.crmInmobiliario.DTO.mpDtos;

import java.math.BigDecimal;

public class SubscriptionRequest {
    private String payerEmail;
    private String planCode;
    private String externalReference;
    private BigDecimal amount;
    private String notificationUrl;

    public SubscriptionRequest() {}

    public SubscriptionRequest(String payerEmail, String planCode, String externalReference,
                               BigDecimal amount, String notificationUrl) {
        this.payerEmail = payerEmail;
        this.planCode = planCode;
        this.externalReference = externalReference;
        this.amount = amount;
        this.notificationUrl = notificationUrl;
    }

    public String getPayerEmail() { return payerEmail; }
    public void setPayerEmail(String payerEmail) { this.payerEmail = payerEmail; }

    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getNotificationUrl() { return notificationUrl; }
    public void setNotificationUrl(String notificationUrl) { this.notificationUrl = notificationUrl; }
}
