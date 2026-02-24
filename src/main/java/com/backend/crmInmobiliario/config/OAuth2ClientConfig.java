package com.backend.crmInmobiliario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2ClientConfig {

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    // Opcional: si no se define, usamos el default de Spring {baseUrl}/login/oauth2/code/{registrationId}
    @Value("${google.login.redirect.uri:{baseUrl}/login/oauth2/code/{registrationId}}")
    private String googleLoginRedirectUri;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration google = googleClientRegistration();
        return new InMemoryClientRegistrationRepository(google);
    }

    private ClientRegistration googleClientRegistration() {


        return ClientRegistration.withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(googleLoginRedirectUri)
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
    }
}
