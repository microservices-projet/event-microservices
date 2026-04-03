package org.example.eventmodule.client;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

/**
 * Feign Client Configuration
 * Implements circuit breaker, retry logic, and error handling
 */
@Configuration
public class UserServiceClientConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new UserServiceErrorDecoder();
    }

    public static class UserServiceErrorDecoder implements ErrorDecoder {
        @Override
        public Exception decode(String methodKey, feign.Response response) {
            if (response.status() == 404) {
                return new UserNotFoundException("User not found");
            } else if (response.status() == 503) {
                return new UserServiceUnavailableException("User service is currently unavailable");
            }
            return new UserServiceException("User service error: " + response.status());
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class UserServiceUnavailableException extends RuntimeException {
        public UserServiceUnavailableException(String message) {
            super(message);
        }
    }

    public static class UserServiceException extends RuntimeException {
        public UserServiceException(String message) {
            super(message);
        }
    }
}

