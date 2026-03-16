package com.backend.crmInmobiliario.DTO.entrada.signature;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignatureOtpDto {
    @NotBlank
    private String otp;
}
