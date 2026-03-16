package com.backend.crmInmobiliario.DTO.salida.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureRequestStatus;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerRoleType;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MySignaturePendingDto {
    private Long signerId;
    private String accessToken;
    private Long requestId;
    private Long contractId;
    private String contractName;
    private SignatureSignerRoleType signerRoleType;
    private SignatureSignerStatus signerStatus;
    private SignatureRequestStatus requestStatus;
    private boolean enabledToSignNow;
    private LocalDateTime requestExpiresAt;
}
