package com.backend.crmInmobiliario.DTO.mpDtos;


import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscriptionResponse {
    private String id;
    private String status;

    @JsonProperty("initPoint")
    private String initPoint;

    public SubscriptionResponse() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInitPoint() {
        return initPoint;
    }

    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }
}

