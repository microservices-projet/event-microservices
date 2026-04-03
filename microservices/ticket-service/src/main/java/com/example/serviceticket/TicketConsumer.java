package com.example.serviceticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketConsumer {

    private static final String TOPIC = "ticket-events";
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC, groupId = "ticket-service-group")
    public void consume(@Payload String payload, 
                       @Header(name = "event-type", required = false) String eventType) {
        try {
            log.info("Kafka consumed from {}: eventType={}, payload={}", TOPIC, eventType, payload);

            JsonNode root = objectMapper.readTree(payload);
            JsonNode n = root.has("payload") ? root.get("payload") : root;
            String effectiveEventType = root.has("eventType") ? root.get("eventType").asText() : eventType;

            if ("CREATED".equals(effectiveEventType) || "UPDATED".equals(effectiveEventType)) {
                Ticket ticket = objectMapper.treeToValue(n, Ticket.class);
                log.info("Processing ticket: id={}, status={}", ticket.getIdTicket(), ticket.getStatut());
            } else if ("DELETED".equals(effectiveEventType)) {
                Long ticketId = n.isNumber() ? n.asLong() : Long.parseLong(n.asText());
                log.info("Processing deleted ticket: id={}", ticketId);
            }
        } catch (Exception e) {
            log.error("Error consuming Kafka message", e);
        }
    }
}
