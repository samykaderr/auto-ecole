package com.autoecole.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.autoecole.user.AppUser;
import com.autoecole.user.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Émission / vérification des JWT (doc §6) : access 15 min + refresh 30 j.
 * Claims de sécurité critiques : schoolId (tenant) et role.
 */
@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-ttl}") Duration accessTtl,
            @Value("${app.jwt.refresh-token-ttl}") Duration refreshTtl) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    public String createAccessToken(AppUser user) {
        return create(user, TYPE_ACCESS, accessTtl);
    }

    public String createRefreshToken(AppUser user) {
        return create(user, TYPE_REFRESH, refreshTtl);
    }

    /**
     * Vérifie la signature et retourne les claims ; lève JwtException si invalide.
     * Le contrôle du type (access / refresh) est à la charge de l'appelant.
     */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public static String claimType(Claims claims) {
        return claims.get("typ", String.class);
    }

    public static UserPrincipal toPrincipal(Claims claims) {
        Role role = Role.valueOf(claims.get("role", String.class));
        return new UserPrincipal(
                Long.valueOf(claims.getSubject()),
                claims.get("schoolId", Long.class),
                claims.get("email", String.class),
                role);
    }

    private String create(AppUser user, String type, Duration ttl) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("typ", type)
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("schoolId", user.getSchoolId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .signWith(key)
                .compact();
    }
}
