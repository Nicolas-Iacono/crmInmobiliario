package com.backend.crmInmobiliario.DTO.entrada.jwt;

public class RefreshResponse {

    private String accessToken;
    private String refreshToken; // puede ser null si no rotás
    private String tokenType;
    private int expiresInSeconds;
    public RefreshResponse() {}
    public RefreshResponse(String accessToken, String refreshToken, String tokenType, int expiresInSeconds) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
    }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public int getExpiresInSeconds() { return expiresInSeconds; }
}
