package org.example.eventmodule.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for User Service Communication
 * Uses Feign for inter-service communication with circuit breaker
 */
@FeignClient(
    name = "user-service",
    url = "${feign.client.config.user-service.url:http://localhost:8081}",
    configuration = UserServiceClientConfig.class,
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * Check if a user exists
     */
    @GetMapping("/api/users/{userId}/exists")
    Boolean userExists(@PathVariable("userId") Long userId);

    /**
     * Get user by ID
     */
    @GetMapping("/api/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") Long userId);

    // DTO class for user data
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    class UserDTO {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
    }
}

/**
 * Fallback implementation for User Service
 */
@Slf4j
@Component
class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Boolean userExists(Long userId) {
        log.warn("User service unavailable, assuming user exists: {}", userId);
        return true;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        log.warn("User service unavailable, returning null for user: {}", userId);
        return null;
    }
}

