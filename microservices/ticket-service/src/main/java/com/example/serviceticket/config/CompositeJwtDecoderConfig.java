package com.example.serviceticket.config;

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

@Configuration
public class CompositeJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String keycloakIssuer,
            @Value("${app.security.local-jwt.secret}") String localSecret,
            @Value("${app.security.local-jwt.issuer:user-service-local}") String localIssuer) {

        SecretKeySpec secretKey = new SecretKeySpec(localSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder local = NimbusJwtDecoder.withSecretKey(secretKey).build();
        local.setJwtValidator(JwtValidators.createDefaultWithIssuer(localIssuer));

        return new LazyCompositeJwtDecoder(keycloakIssuer, local);
    }

    private static final class LazyCompositeJwtDecoder implements JwtDecoder {

        private final String keycloakIssuer;
        private final NimbusJwtDecoder localDecoder;
        private volatile NimbusJwtDecoder keycloakDecoder;

        LazyCompositeJwtDecoder(String keycloakIssuer, NimbusJwtDecoder localDecoder) {
            this.keycloakIssuer = keycloakIssuer;
            this.localDecoder = localDecoder;
        }

        private NimbusJwtDecoder resolveKeycloakDecoder() {
            NimbusJwtDecoder d = keycloakDecoder;
            if (d != null) {
                return d;
            }
            synchronized (this) {
                if (keycloakDecoder == null) {
                    keycloakDecoder = NimbusJwtDecoder.withIssuerLocation(keycloakIssuer).build();
                }
                return keycloakDecoder;
            }
        }

        @Override
        public Jwt decode(String token) throws JwtException {
            NimbusJwtDecoder kc = null;
            try {
                kc = resolveKeycloakDecoder();
            } catch (RuntimeException ignored) {
                // Keycloak not reachable yet
            }
            if (kc != null) {
                try {
                    return kc.decode(token);
                } catch (JwtException ex) {
                    return localDecoder.decode(token);
                }
            }
            return localDecoder.decode(token);
        }
    }
}
