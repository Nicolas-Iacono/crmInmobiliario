package com.backend.crmInmobiliario.DTO.mpDtos;

public class CheckoutRequest {
    private String planCode;


    public CheckoutRequest() {}


    public CheckoutRequest(String planCode) {
        this.planCode = planCode;
    }


    public String getPlanCode() {
        return planCode;
    }


    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }
}
