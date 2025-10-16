package com.backend.crmInmobiliario.entity.planesYSuscripciones;


import jakarta.persistence.*;
import java.math.BigDecimal;


@Entity
@Table(name = "plans")
public class Plan {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String code; // Por ejemplo, "PROFESIONAL", "BASICO"
    private String name;

    @Column(name = "price_ars")
    private BigDecimal priceArs; // Precio en ARS (la moneda local configurada)

    @Column(name = "price_usd")
    private BigDecimal priceUsd; // Nuevo: Precio en USD (si se usa en lógica de negocio)

    // --- Campos de configuración de Mercado Pago (Auto Recurring) ---
    @Column(name = "mp_frequency")
    private Integer frequency; // Frecuencia: 1, 3, 6, etc.

    @Column(name = "mp_frequency_type", length = 10)
    private String frequencyType; // Tipo de frecuencia: 'months' o 'days'

    @Column(name = "mp_currency_id", length = 3)
    private String currencyId; // ID de la moneda: ARS, BRL, etc.
    // -------------------------------------------------------------

    private Integer contractLimit; // Límite de contratos por plan
    private boolean active;
    private String externalPlanId; // ID externo de MP (ID de preapproval_plan)


    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }


    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }


    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }


    public Integer getContractLimit() { return contractLimit; }
    public void setContractLimit(Integer contractLimit) { this.contractLimit = contractLimit; }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }


    public BigDecimal getPriceArs() { return priceArs; }
    public void setPriceArs(BigDecimal priceArs) { this.priceArs = priceArs; }

    public BigDecimal getPriceUsd() { return priceUsd; }
    public void setPriceUsd(BigDecimal priceUsd) { this.priceUsd = priceUsd; }


    public String getExternalPlanId() { return externalPlanId; }
    public void setExternalPlanId(String externalPlanId) { this.externalPlanId = externalPlanId; }


    // Getters y Setters para Campos de MP
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

    public String getCurrencyId() {
        return currencyId;
    }
    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }
}
