package com.example.serviceticket;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class UserInternalFeignConfig {

    @Bean
    public RequestInterceptor userServiceInternalKeyInterceptor(
            @Value("${app.user-service.inter-service-key:}") String key) {
        return template -> {
            if (key != null && !key.isBlank()) {
                template.header("X-Internal-Service-Key", key);
            }
        };
    }
}
