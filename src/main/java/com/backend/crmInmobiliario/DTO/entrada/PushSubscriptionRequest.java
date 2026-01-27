package com.backend.crmInmobiliario.DTO.entrada;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PushSubscriptionRequest {

    private String endpoint;
    private Long expirationTime;
    private Keys keys;

    @Data
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
