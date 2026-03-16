package com.backend.crmInmobiliario.DTO.salida.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureRequestStatus;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerRoleType;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SignaturePublicAccessDto {
    private Long requestId;
    private Long contractId;
    private SignatureRequestStatus requestStatus;
    private SignatureSignerStatus signerStatus;
    private SignatureSignerRoleType signerRoleType;
    private Long relatedEntityId;
    private String fullName;
    private String email;
    private boolean sequentialSigning;
    private Integer signOrder;
    private boolean enabledToSignNow;
    private LocalDateTime requestExpiresAt;
    private LocalDateTime accessTokenExpiresAt;
    private String pdfOriginalUrl;
}
