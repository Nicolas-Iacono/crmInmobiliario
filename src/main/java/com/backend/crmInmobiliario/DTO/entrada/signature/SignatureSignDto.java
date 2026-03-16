package com.backend.crmInmobiliario.DTO.entrada.signature;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignatureSignDto {
    @NotNull
    private Boolean consentAccepted;

    private String consentText;

    private String signatureDrawDataJson;

    private String signatureImageBase64;
}
