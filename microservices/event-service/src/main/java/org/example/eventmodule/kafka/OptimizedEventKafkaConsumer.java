package org.example.eventmodule.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eventmodule.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * Optimized Event Kafka Consumer
 * Handles all event-related CRUD messages from User and other services
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OptimizedEventKafkaConsumer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String EVENTS_TOPIC = "optimized-event-stream";
    private static final String AGGREGATE_TYPE = "Event";

    /**
     * Send CREATE event
     */
    public void sendEventCreated(Event event) {
        sendEventMessage(event, "CREATED");
    }

    /**
     * Send READ/FETCH event
     */
    public void sendEventFetched(Event event) {
        sendEventMessage(event, "FETCHED");
    }

    /**
     * Send UPDATE event
     */
    public void sendEventUpdated(Event event) {
        sendEventMessage(event, "UPDATED");
    }

    /**
     * Send DELETE event
     */
    public void sendEventDeleted(Long eventId, String eventTitle) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    "DELETED",
                    AGGREGATE_TYPE,
                    String.valueOf(eventId),
                    new EventDeletedPayload(eventId, eventTitle),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    null
            );

            String payload = objectMapper.writeValueAsString(envelope);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, EVENTS_TOPIC)
                    .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, "DELETED")
                    .setHeader("timestamp", System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send(message);
            log.info("✓ Event DELETED published: eventId={}", eventId);
        } catch (Exception e) {
            log.error("✗ Failed to publish event DELETE for eventId {}: {}", eventId, e.getMessage());
        }
    }

    /**
     * Send LIKE event
     */
    public void sendEventLiked(Long eventId, Long userId, int totalLikes) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    "LIKED",
                    AGGREGATE_TYPE,
                    String.valueOf(eventId),
                    new EventLikedPayload(eventId, userId, totalLikes),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    null
            );

            String payload = objectMapper.writeValueAsString(envelope);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, EVENTS_TOPIC)
                    .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, "LIKED")
                    .setHeader("timestamp", System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send(message);
            log.info("✓ Event LIKED published: eventId={}, userId={}", eventId, userId);
        } catch (Exception e) {
            log.error("✗ Failed to publish event LIKED for eventId {}: {}", eventId, e.getMessage());
        }
    }

    /**
     * Send SEARCH event
     */
    public void sendEventSearched(String keyword, int resultCount) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    "SEARCHED",
                    "EventSearch",
                    UUID.randomUUID().toString(),
                    new EventSearchedPayload(keyword, resultCount),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    null
            );

            String payload = objectMapper.writeValueAsString(envelope);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, EVENTS_TOPIC)
                    .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, "SEARCHED")
                    .setHeader("timestamp", System.currentTimeMillis())
                    .build();
            
            kafkaTemplate.send(message);
            log.info("✓ Event SEARCHED published: keyword={}, results={}", keyword, resultCount);
        } catch (Exception e) {
            log.error("✗ Failed to publish event SEARCHED: {}", e.getMessage());
        }
    }

    private void sendEventMessage(Event event, String eventType) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    eventType,
                    AGGREGATE_TYPE,
                    String.valueOf(event.getId()),
                    event,
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    null
            );

            String payload = objectMapper.writeValueAsString(envelope);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, EVENTS_TOPIC)
                    .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, eventType)
                    .setHeader("timestamp", System.currentTimeMillis())
                    .setHeader("aggregateId", String.valueOf(event.getId()))
                    .build();
            
            kafkaTemplate.send(message);
            log.info("✓ Event {} published: eventId={}", eventType, event.getId());
        } catch (Exception e) {
            log.error("✗ Failed to publish event {} for eventId {}: {}", eventType, event.getId(), e.getMessage());
        }
    }

    // Payload classes for specific events
    static class EventDeletedPayload {
        public Long eventId;
        public String eventTitle;

        EventDeletedPayload(Long eventId, String eventTitle) {
            this.eventId = eventId;
            this.eventTitle = eventTitle;
        }
    }

    static class EventLikedPayload {
        public Long eventId;
        public Long userId;
        public int totalLikes;

        EventLikedPayload(Long eventId, Long userId, int totalLikes) {
            this.eventId = eventId;
            this.userId = userId;
            this.totalLikes = totalLikes;
        }
    }

    static class EventSearchedPayload {
        public String keyword;
        public int resultCount;

        EventSearchedPayload(String keyword, int resultCount) {
            this.keyword = keyword;
            this.resultCount = resultCount;
        }
    }
}

