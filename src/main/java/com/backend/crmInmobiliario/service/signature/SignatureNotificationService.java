package com.backend.crmInmobiliario.service.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureSigner;

public interface SignatureNotificationService {
    void notifySignerInvitation(SignatureSigner signer);

    void notifyAgencySignerSigned(SignatureSigner signer);

    void notifyAgencyRequestCompleted(SignatureSigner signer);
}
