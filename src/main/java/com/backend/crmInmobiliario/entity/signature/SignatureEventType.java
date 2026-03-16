package com.backend.crmInmobiliario.entity.signature;

public enum SignatureEventType {
    REQUEST_CREATED,
    LINK_OPENED,
    PDF_VIEWED,
    CONSENT_ACCEPTED,
    OTP_SENT,
    OTP_FAILED,
    OTP_VERIFIED,
    SIGN_ATTEMPT,
    SIGNED,
    REJECTED,
    EXPIRED
}
