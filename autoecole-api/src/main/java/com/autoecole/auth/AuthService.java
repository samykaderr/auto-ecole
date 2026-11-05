package com.autoecole.auth;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.autoecole.auth.dto.LoginRequest;
import com.autoecole.auth.dto.TokenResponse;
import com.autoecole.security.JwtService;
import com.autoecole.user.AppUser;
import com.autoecole.user.AppUserRepository;

/**
 * Authentification par email + mot de passe haché BCrypt (doc §9).
 * TODO V1.1 : refresh tokens persistés (révocables) + gestion multi-écoles pour un même email.
 */
@Service
public class AuthService {

    private final AppUserRepository users;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Duration accessTtl;

    public AuthService(AppUserRepository users, JwtService jwtService, PasswordEncoder passwordEncoder,
                       @Value("${app.jwt.access-token-ttl}") Duration accessTtl) {
        this.users = users;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.accessTtl = accessTtl;
    }

    public TokenResponse login(LoginRequest request) {
        List<AppUser> matches =
                users.findByEmailIgnoreCaseAndActiveTrue(request.email()).stream().limit(2).toList();
        if (matches.size() != 1) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        }
        AppUser user = matches.get(0);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        }
        return TokenResponse.of(
                jwtService.createAccessToken(user),
                jwtService.createRefreshToken(user),
                accessTtl.toSeconds());
    }

    public TokenResponse refresh(String refreshToken) {
        io.jsonwebtoken.Claims claims = jwtService.parse(refreshToken);
        if (!JwtService.TYPE_REFRESH.equals(JwtService.claimType(claims))) {
            throw new BadCredentialsException("Refresh token invalide.");
        }
        AppUser user = users.findById(Long.valueOf(claims.getSubject()))
                .filter(AppUser::isActive)
                .orElseThrow(() -> new BadCredentialsException("Compte inexistant ou désactivé."));
        return TokenResponse.of(
                jwtService.createAccessToken(user),
                jwtService.createRefreshToken(user),
                accessTtl.toSeconds());
    }
}
