package com.backend.crmInmobiliario.service.signature;

import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerRoleType;

public interface SignatureSignerResolverService {
    SignatureResolvedSignerData resolveAndValidate(Contrato contract,
                                                   SignatureSignerRoleType signerRoleType,
                                                   Long relatedEntityId);
}
