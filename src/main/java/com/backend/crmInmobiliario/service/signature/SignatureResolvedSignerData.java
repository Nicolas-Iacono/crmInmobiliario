package com.backend.crmInmobiliario.service.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureSignerRoleType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SignatureResolvedSignerData {
    SignatureSignerRoleType signerRoleType;
    Long relatedEntityId;
    String fullName;
    String email;
    String phone;
    String dni;
}
