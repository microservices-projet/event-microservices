package com.saladin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Accepts Keycloak JWTs first, then local HS256 tokens issued by {@link com.saladin.security.LocalJwtService}.
 * Keycloak {@link NimbusJwtDecoder} is created lazily on first decode so startup is not blocked on OIDC metadata fetch.
 */
@Configuration
public class CompositeJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String keycloakIssuer,
            @Value("${app.security.local-jwt.secret}") String localSecret,
            @Value("${app.security.local-jwt.issuer:user-service-local}") String localIssuer) {

        JwtDecoder keycloak = null;
        if (keycloakIssuer != null && !keycloakIssuer.isBlank()) {
            keycloak = new LazyIssuerJwtDecoder(keycloakIssuer);
        }

        SecretKeySpec secretKey = new SecretKeySpec(localSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder local = NimbusJwtDecoder.withSecretKey(secretKey).build();
        local.setJwtValidator(JwtValidators.createDefaultWithIssuer(localIssuer));

        final JwtDecoder keycloakDecoder = keycloak;
        return token -> {
            if (keycloakDecoder != null) {
                try {
                    return keycloakDecoder.decode(token);
                } catch (JwtException ex) {
                    return local.decode(token);
                }
            }
            return local.decode(token);
        };
    }

    /**
     * Defers {@link NimbusJwtDecoder#withIssuerLocation(String)} (network) until the first token is decoded.
     */
    private static final class LazyIssuerJwtDecoder implements JwtDecoder {
        private final String issuerLocation;
        private volatile JwtDecoder delegate;

        private LazyIssuerJwtDecoder(String issuerLocation) {
            this.issuerLocation = issuerLocation;
        }

        @Override
        public Jwt decode(String token) throws JwtException {
            JwtDecoder d = delegate;
            if (d == null) {
                synchronized (this) {
                    d = delegate;
                    if (d == null) {
                        try {
                            d = NimbusJwtDecoder.withIssuerLocation(issuerLocation).build();
                            delegate = d;
                        } catch (RuntimeException ex) {
                            throw new JwtException("Could not resolve JWT from issuer " + issuerLocation, ex);
                        }
                    }
                }
            }
            return d.decode(token);
        }
    }
}
