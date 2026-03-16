package com.backend.crmInmobiliario.DTO.salida.signature;

import com.backend.crmInmobiliario.entity.signature.SignatureRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SignatureRequestResponseDto {
    private Long id;
    private Long contractId;
    private Long createdByUserId;
    private String pdfHashSha256;
    private String pdfOriginalUrl;
    private SignatureRequestStatus status;
    private boolean sequentialSigning;
    private LocalDateTime expiresAt;
    private List<SignatureSignerResponseDto> signers;
}
