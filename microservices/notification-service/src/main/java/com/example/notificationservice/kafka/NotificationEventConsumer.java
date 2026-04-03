package com.example.notificationservice.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private static final String AUDIT_TOPIC = "audit-events";

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(
            topics = {
                    "user-events",
                    "reservation-events",
                    "ticket-events",
                    "feedback-events",
                    "reclamation-events",
                    "saga-events",
                    "analytics-alerts"
            },
            groupId = "notification-service-group"
    )
    public void onEvent(
            @Payload String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            JsonNode envelope = objectMapper.readTree(payload);
            String eventId = envelope.hasNonNull("eventId") ? envelope.get("eventId").asText() : null;
            String eventType = envelope.hasNonNull("eventType") ? envelope.get("eventType").asText() : null;

            // 1) Notifications (MVP: log only)
            log.info("Notification candidate topic={} eventType={} eventId={}", topic, eventType, eventId);

            // Example: reservation confirmed
            if ("reservation-events".equals(topic) && "CONFIRMED".equals(eventType)) {
                JsonNode reservation = envelope.get("payload");
                String userId = reservation != null && reservation.hasNonNull("userId") ? reservation.get("userId").asText() : null;
                log.info("Reservation confirmed notification (userId={})", userId);
            }

            // 2) Audit: publish a traceable record for every consumed domain event.
            ObjectNode audit = objectMapper.createObjectNode();
            audit.put("auditEventId", UUID.randomUUID().toString());
            audit.put("sourceTopic", topic);
            audit.put("sourceEventId", eventId);
            audit.put("sourceEventType", eventType);
            audit.put("auditedAt", Instant.now().toString());
            audit.set("sourceEnvelope", envelope);

            String auditJson = objectMapper.writeValueAsString(audit);
            kafkaTemplate.send(AUDIT_TOPIC, auditJson);
        } catch (Exception e) {
            // Let the message fail loudly: this service doesn't have DLQ wiring yet.
            log.error("Notification/audit processing failed (topic={}): {}", topic, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

