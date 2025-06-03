package com.pln.trafo.service;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.JWTParser;
import com.pln.trafo.entity.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    @Inject
    JWTParser jwtParser;

    private static final Duration TOKEN_DURATION = Duration.ofDays(1); // 24 hours

    public String generateToken(User user) {
        return Jwt.issuer("https://pln-trafo.com")
                .upn(user.username)
                .groups(Set.of(user.role.name()))
                .claim("userId", user.id)
                .claim("email", user.email)
                .claim("role", user.role.name())
                .expiresAt(Instant.now().plus(TOKEN_DURATION))
                .sign();
    }

    public String generateRefreshToken(User user) {
        return Jwt.issuer("https://pln-trafo.com")
                .upn(user.username)
                .claim("userId", user.id)
                .claim("type", "refresh")
                .expiresAt(Instant.now().plus(Duration.ofDays(7))) // 7 days for refresh token
                .sign();
    }

    public Long getTokenExpirationTime() {
        return TOKEN_DURATION.toSeconds();
    }

    public boolean isTokenValid(String token) {
        try {
            jwtParser.parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
