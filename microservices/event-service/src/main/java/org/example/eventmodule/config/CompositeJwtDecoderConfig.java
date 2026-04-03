package org.example.eventmodule.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String keycloakIssuer,
            @Value("${app.security.local-jwt.secret}") String localSecret,
            @Value("${app.security.local-jwt.issuer:user-service-local}") String localIssuer) {

        JwtDecoder keycloak = null;
        if (keycloakIssuer != null && !keycloakIssuer.isBlank()) {
            try {
                keycloak = NimbusJwtDecoder.withIssuerLocation(keycloakIssuer).build();
            } catch (Exception ex) {
                // Keycloak is not available, will only use local JWT decoder
                System.err.println("Warning: Could not initialize Keycloak decoder. Only local JWTs will be accepted: " + ex.getMessage());
            }
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
}
