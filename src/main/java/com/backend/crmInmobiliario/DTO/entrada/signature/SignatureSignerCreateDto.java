package com.backend.crmInmobiliario.DTO.entrada.signature;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignatureSignerCreateDto {
    @NotBlank
    private String signerRoleType;

    @NotNull
    private Long relatedEntityId;

    private Integer signOrder;
}
