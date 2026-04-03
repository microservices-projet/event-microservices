package com.example.sagaorchestrator.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.sagaorchestrator.dto.SagaReserveRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventPublisher {

    public static final String TOPIC = "saga-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:saga-orchestrator-service}")
    private String applicationName;

    public void publish(
            String sagaId,
            String sagaEventType,
            SagaReserveRequest request,
            Long ticketId,
            String reservationId,
            String detail
    ) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sagaId", sagaId);
            payload.put("userId", request.getUserId());
            payload.put("eventId", request.getEventId());
            payload.put("places", request.getPlaces());
            payload.put("reservationId", reservationId);
            payload.put("ticketId", ticketId);
            payload.put("detail", detail);

            EventEnvelope envelope = EventEnvelope.v1(
                    sagaEventType,
                    "Saga",
                    sagaId,
                    payload,
                    sagaId,
                    null,
                    applicationName
            );
            kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(envelope));
            log.info("Published saga event type={} sagaId={}", sagaEventType, sagaId);
        } catch (Exception e) {
            log.error("Failed to publish saga event: {}", e.getMessage(), e);
        }
    }
}
