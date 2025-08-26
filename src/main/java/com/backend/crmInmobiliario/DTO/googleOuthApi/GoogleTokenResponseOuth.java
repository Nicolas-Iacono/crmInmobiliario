package com.backend.crmInmobiliario.DTO.googleOuthApi;

public record GoogleTokenResponseOuth(
        String access_token,
        Long expires_in,
        String refresh_token,
        String scope,
        String token_type,
        String id_token
) {
}
