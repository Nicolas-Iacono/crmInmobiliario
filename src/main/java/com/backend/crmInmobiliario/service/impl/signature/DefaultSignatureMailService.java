package com.backend.crmInmobiliario.service.impl.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureSigner;
import com.backend.crmInmobiliario.service.signature.SignatureMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultSignatureMailService implements SignatureMailService {

    @Override
    public void sendOtp(SignatureSigner signer, String otp) {
        // Adaptar para integrarse con tu servicio real de correos.
        log.info("OTP para firma enviado (simulado): signerId={}, email={}, otp={}", signer.getId(), signer.getEmail(), otp);
    }
}
