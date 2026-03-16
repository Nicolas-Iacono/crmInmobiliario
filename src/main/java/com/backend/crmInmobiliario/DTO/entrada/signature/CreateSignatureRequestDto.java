package com.backend.crmInmobiliario.DTO.entrada.signature;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateSignatureRequestDto {
    @NotNull
    private Long contractId;

    @NotNull
    private Boolean sequentialSigning;

    @NotNull
    private Integer expiresInHours;

    @Valid
    @NotEmpty
    private List<SignatureSignerCreateDto> signers;
}
