package com.example.sagaorchestrator;

import com.example.sagaorchestrator.config.FeignAuthConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = FeignAuthConfig.class)
public class SagaOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SagaOrchestratorServiceApplication.class, args);
    }
}

