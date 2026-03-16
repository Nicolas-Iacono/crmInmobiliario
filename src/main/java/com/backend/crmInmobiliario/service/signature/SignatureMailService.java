package com.backend.crmInmobiliario.service.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureSigner;

public interface SignatureMailService {
    void sendOtp(SignatureSigner signer, String otp);
}
