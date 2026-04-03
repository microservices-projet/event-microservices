package com.example.reclamationservice.kafka;

import com.example.eventcontract.EventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReferenceProjectionConsumer {

    private final ObjectMapper objectMapper;
    private final ReferenceProjectionStore projectionStore;

    @KafkaListener(topics = "user-events", groupId = "reclamation-reference-projections")
    public void onUserEvent(String payload) {
        try {
            EventEnvelope env = objectMapper.readValue(payload, EventEnvelope.class);
            JsonNode n = objectMapper.valueToTree(env.getPayload());
            Long id = firstLong(n, "id", env.getAggregateId());
            if ("DELETED".equals(env.getEventType())) {
                projectionStore.removeUser(id);
                log.info("Projection updated: removed user {}", id);
                return;
            }
            projectionStore.upsertUsername(id, text(n, "username"));
            log.info("Projection updated: user {} -> {}", id, text(n, "username"));
        } catch (Exception e) {
            log.warn("Failed user-events projection in reclamation-service: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "event-events", groupId = "reclamation-reference-projections")
    public void onEventEvent(String payload) {
        try {
            EventEnvelope env = objectMapper.readValue(payload, EventEnvelope.class);
            JsonNode n = objectMapper.valueToTree(env.getPayload());
            Long id = firstLong(n, "id", env.getAggregateId());
            if ("DELETED".equals(env.getEventType())) {
                projectionStore.removeEvent(id);
                log.info("Projection updated: removed event {}", id);
                return;
            }
            projectionStore.upsertEventTitle(id, text(n, "title"));
            log.info("Projection updated: event {} -> {}", id, text(n, "title"));
        } catch (Exception e) {
            log.warn("Failed event-events projection in reclamation-service: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "ticket-events", groupId = "reclamation-reference-projections")
    public void onTicketEvent(String payload) {
        try {
            EventEnvelope env = objectMapper.readValue(payload, EventEnvelope.class);
            JsonNode n = objectMapper.valueToTree(env.getPayload());
            Long ticketId = firstLong(n, "idTicket", env.getAggregateId());
            if ("DELETED".equals(env.getEventType())) {
                projectionStore.removeTicket(ticketId);
                return;
            }
            String label = text(n, "eventTitle");
            if (label == null || label.isBlank()) {
                label = text(n, "typeTicket");
            }
            projectionStore.upsertTicketLabel(ticketId, label);
        } catch (Exception e) {
            log.warn("Failed ticket-events projection in reclamation-service: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "reservation-events", groupId = "reclamation-reference-projections")
    public void onReservationEvent(String payload) {
        try {
            EventEnvelope env = objectMapper.readValue(payload, EventEnvelope.class);
            JsonNode n = objectMapper.valueToTree(env.getPayload());
            String reservationId = firstText(n, "id", env.getAggregateId());
            if ("DELETED".equals(env.getEventType())) {
                projectionStore.removeReservation(reservationId);
                return;
            }
            String status = text(n, "status");
            String eventId = firstText(n, "eventId", null);
            String label = "status=" + (status == null ? "UNKNOWN" : status)
                    + (eventId == null ? "" : ", eventId=" + eventId);
            projectionStore.upsertReservationLabel(reservationId, label);
        } catch (Exception e) {
            log.warn("Failed reservation-events projection in reclamation-service: {}", e.getMessage());
        }
    }

    private String text(JsonNode n, String key) {
        if (n == null || n.isNull() || !n.has(key)) return null;
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private Long firstLong(JsonNode n, String payloadKey, String fallbackText) {
        if (n != null && n.has(payloadKey) && !n.get(payloadKey).isNull()) {
            return n.get(payloadKey).asLong();
        }
        if (fallbackText == null || fallbackText.isBlank()) return null;
        try {
            return Long.parseLong(fallbackText);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String firstText(JsonNode n, String payloadKey, String fallbackText) {
        if (n != null && n.has(payloadKey) && !n.get(payloadKey).isNull()) {
            return n.get(payloadKey).asText();
        }
        return fallbackText;
    }
}
