package com.backend.crmInmobiliario.DTO.entrada.N8N;

import lombok.Data;

@Data
public class AysaWebhookRequest {
    private String requestId;
    private String cuentaServicios;
    private String email;
    private Long contratoId;
}
