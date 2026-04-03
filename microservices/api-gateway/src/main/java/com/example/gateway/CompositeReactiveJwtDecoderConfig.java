package com.example.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class CompositeReactiveJwtDecoderConfig {

    private static final Logger log = LoggerFactory.getLogger(CompositeReactiveJwtDecoderConfig.class);

    @Bean
    @ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.jwt.issuer-uri")
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String keycloakIssuer,
            @Value("${app.security.local-jwt.secret:}") String localSecret,
            @Value("${app.security.local-jwt.issuer:user-service-local}") String localIssuer) {

        log.info("Configuring Composite ReactiveJwtDecoder");

        ReactiveJwtDecoder keycloakDecoder = null;
        ReactiveJwtDecoder localDecoder = null;

        // Configure Keycloak decoder if issuer URI is provided
        if (keycloakIssuer != null && !keycloakIssuer.isEmpty()) {
            try {
                keycloakDecoder = NimbusReactiveJwtDecoder.withIssuerLocation(keycloakIssuer).build();
                log.info("Keycloak JWT decoder configured with issuer: {}", keycloakIssuer);
            } catch (Exception e) {
                log.error("Failed to configure Keycloak JWT decoder: {}", e.getMessage());
            }
        } else {
            log.warn("Keycloak issuer URI not configured. Keycloak JWT validation will be disabled.");
        }

        // Configure local decoder if secret is provided
        if (localSecret != null && !localSecret.isEmpty()) {
            try {
                SecretKeySpec secretKey = new SecretKeySpec(localSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                localDecoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
                ((NimbusReactiveJwtDecoder) localDecoder).setJwtValidator(JwtValidators.createDefaultWithIssuer(localIssuer));
                log.info("Local JWT decoder configured with issuer: {}", localIssuer);
            } catch (Exception e) {
                log.error("Failed to configure local JWT decoder: {}", e.getMessage());
            }
        } else {
            log.warn("Local JWT secret not configured. Local JWT validation will be disabled.");
        }

        // Create composite decoder based on available decoders
        final ReactiveJwtDecoder finalKeycloakDecoder = keycloakDecoder;
        final ReactiveJwtDecoder finalLocalDecoder = localDecoder;

        if (finalKeycloakDecoder != null && finalLocalDecoder != null) {
            // Both decoders available - try local first to avoid remote issuer latency for local JWTs.
            return token -> finalLocalDecoder.decode(token)
                    .onErrorResume(JwtException.class, e -> {
                        log.debug("Local JWT validation failed, trying Keycloak decoder: {}", e.getMessage());
                        return finalKeycloakDecoder.decode(token);
                    });
        } else if (finalKeycloakDecoder != null) {
            // Only Keycloak decoder available
            log.info("Using only Keycloak JWT decoder");
            return finalKeycloakDecoder;
        } else if (finalLocalDecoder != null) {
            // Only local decoder available
            log.info("Using only local JWT decoder");
            return finalLocalDecoder;
        } else {
            // No decoder available - throw configuration exception
            log.error("No JWT decoder configured. Please configure either Keycloak issuer URI or local JWT secret.");
            throw new IllegalStateException(
                    "No JWT decoder configured. Please configure " +
                            "spring.security.oauth2.resourceserver.jwt.issuer-uri for Keycloak or " +
                            "app.security.local-jwt.secret for local JWT validation"
            );
        }
    }
}