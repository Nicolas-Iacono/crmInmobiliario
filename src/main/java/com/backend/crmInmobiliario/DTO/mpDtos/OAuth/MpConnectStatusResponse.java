package com.backend.crmInmobiliario.DTO.mpDtos.OAuth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MpConnectStatusResponse {
    private boolean connected;
    private String mpAccountEmail; // si después lo querés guardar
    private LocalDateTime tokenExpiresAt;
}