package com.autoecole.auth.dto;

/**
 * Access token 15 min et refresh token 30 j (doc §6).
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn) {

    public static TokenResponse of(String accessToken, String refreshToken, long accessTtlSeconds) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", accessTtlSeconds);
    }
}
