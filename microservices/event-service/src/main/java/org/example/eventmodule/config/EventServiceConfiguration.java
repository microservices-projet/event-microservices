package org.example.eventmodule.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Feign clients for outbound calls (e.g. user-service).
 */
@Configuration
@EnableFeignClients(basePackages = "org.example.eventmodule.client")
public class EventServiceConfiguration {
}

