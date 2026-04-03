package com.example.gateway;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service routes
                .route("user-service", r -> r
                        .path("/auth/**", "/api/users/**")
                        .uri("lb://USER-SERVICE"))

                // Event Service — public demo (no JWT), forwarded to event-service
                .route("event-service-public", r -> r
                        .path("/public", "/public/**")
                        .uri("lb://EVENT-SERVICE"))

                // Event Service routes
                .route("event-service", r -> r
                        .path("/api/events", "/api/events/**", "/api/v1/events", "/api/v1/events/**")
                        .uri("lb://EVENT-SERVICE"))

                // Ticket Service routes
                .route("ticket-service", r -> r
                        .path("/api/tickets/**")
                        .uri("lb://TICKET-SERVICE"))

                // Feedback Service routes
                .route("feedback-service", r -> r
                        .path("/api/feedbacks/**")
                        .uri("lb://FEEDBACK-SERVICE"))

                // Reclamation Service routes
                .route("reclamation-service", r -> r
                        .path("/api/reclamations/**")
                        .uri("lb://RECLAMATION-SERVICE"))

                // Reservation Service routes
                .route("reservation-service", r -> r
                        .path("/api/reservations/**")
                        .uri("lb://RESERVATION-SERVICE"))

                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("lb://PAYMENT-SERVICE"))

                .route("saga-orchestrator-service", r -> r
                        .path("/api/saga/**")
                        .uri("lb://SAGA-ORCHESTRATOR-SERVICE"))

                .route("analytics-streams-service", r -> r
                        .path("/api/analytics/**")
                        .uri("lb://ANALYTICS-STREAMS-SERVICE"))

                .build();
    }
}
