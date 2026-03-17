package com.backend.crmInmobiliario.entity.signature;

public enum SignatureSignerStatus {
    PENDING,
    OTP_SENT,
    OTP_VERIFIED,
    SIGNED,
    REJECTED,
    EXPIRED
}
