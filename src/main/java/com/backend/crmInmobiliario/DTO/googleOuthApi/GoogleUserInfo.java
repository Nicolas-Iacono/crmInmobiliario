package com.backend.crmInmobiliario.DTO.googleOuthApi;

public record GoogleUserInfo(
        String sub,
        String email,
        Boolean email_verified,
        String name,
        String given_name,
        String family_name,
        String picture,
        String locale
) {
}
