package com.saladin.security;

import com.saladin.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Signs short-lived JWTs for local username/password auth so the API Gateway can accept
 * {@code Authorization: Bearer} the same way as Keycloak tokens.
 */
@Service
public class LocalJwtService {

    private final SecretKey key;
    private final String issuer;

    public LocalJwtService(
            @Value("${app.security.local-jwt.secret}") String secret,
            @Value("${app.security.local-jwt.issuer:user-service-local}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getUsername())
                .claim("realm_access", Map.of("roles", List.of(user.getRole().name())))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(24, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }
}
