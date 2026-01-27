package com.backend.crmInmobiliario.DTO.entrada;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String email;
    private String token;
    private String newPassword;
}
