package com.backend.crmInmobiliario.DTO.mpDtos;

public class CheckoutResponse {
    private String redirectUrl;


    public CheckoutResponse() {}


    public CheckoutResponse(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }


    public String getRedirectUrl() {
        return redirectUrl;
    }


    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}