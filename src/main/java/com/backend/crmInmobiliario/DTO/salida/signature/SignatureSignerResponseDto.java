package com.backend.crmInmobiliario.DTO.salida.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureSignerRoleType;
import com.backend.crmInmobiliario.entity.signature.SignatureSignerStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SignatureSignerResponseDto {
    private Long id;
    private SignatureSignerRoleType signerRoleType;
    private Long relatedEntityId;
    private String fullName;
    private String email;
    private String phone;
    private String dni;
    private Integer signOrder;
    private SignatureSignerStatus status;
    private LocalDateTime signedAt;
}
