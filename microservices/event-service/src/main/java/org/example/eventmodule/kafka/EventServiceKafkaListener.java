package org.example.eventmodule.kafka;

import com.example.eventcontract.EventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

/**
 * Event Service Kafka Listener
 * Consumes events from User Service and other microservices
 * Implements retry logic and error handling
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceKafkaListener {

    private final ObjectMapper objectMapper;

    /**
     * Listen for User events (CREATE, UPDATE, DELETE)
     * Topic: user-events
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
        include = {Exception.class}
    )
    @KafkaListener(topics = "user-events", groupId = "event-service-user-group")
    public void handleUserEvent(String eventPayload) {
        try {
            log.info("→ Received User Event: {}", eventPayload.substring(0, Math.min(100, eventPayload.length())));

            EventEnvelope envelope = objectMapper.readValue(eventPayload, EventEnvelope.class);
            UserEventMessage event = mapUserEvent(envelope);

            switch (event.eventType) {
                case "CREATED":
                    handleUserCreated(event);
                    break;
                case "UPDATED":
                    handleUserUpdated(event);
                    break;
                case "DELETED":
                    handleUserDeleted(event);
                    break;
                default:
                    log.warn("⚠ Unknown user event type: {}", event.eventType);
            }
            
            log.info("✓ User event processed successfully");
        } catch (Exception e) {
            log.error("✗ Error processing user event: {}", e.getMessage());
            throw new RuntimeException("Failed to process user event", e);
        }
    }

    private UserEventMessage mapUserEvent(EventEnvelope envelope) {
        UserEventMessage event = new UserEventMessage();
        event.eventType = envelope.getEventType();

        JsonNode payload = objectMapper.valueToTree(envelope.getPayload());
        event.userId = extractUserId(payload, envelope.getAggregateId());
        event.username = text(payload, "username");
        event.email = text(payload, "email");
        return event;
    }

    private Long extractUserId(JsonNode payload, String aggregateId) {
        Long fromPayload = longVal(payload, "id");
        if (fromPayload != null) {
            return fromPayload;
        }
        if (payload != null && payload.isNumber()) {
            return payload.asLong();
        }
        if (aggregateId == null || aggregateId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(aggregateId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * Listen for Feedback events (affecting event ratings)
     * Topic: feedback-events
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "feedback-events", groupId = "event-service-feedback-group")
    public void handleFeedbackEvent(String eventPayload) {
        try {
            log.info("→ Received Feedback Event");
            
            FeedbackEventMessage event = objectMapper.readValue(eventPayload, FeedbackEventMessage.class);
            
            switch (event.eventType) {
                case "CREATED":
                    handleFeedbackCreated(event);
                    break;
                case "MODERATED":
                    handleFeedbackModerated(event);
                    break;
                case "DELETED":
                    handleFeedbackDeleted(event);
                    break;
                default:
                    log.warn("⚠ Unknown feedback event type: {}", event.eventType);
            }
            
            log.info("✓ Feedback event processed successfully");
        } catch (Exception e) {
            log.error("✗ Error processing feedback event: {}", e.getMessage());
            throw new RuntimeException("Failed to process feedback event", e);
        }
    }

    /**
     * Listen for Reservation events (affecting event availability)
     * Topic: reservation-events
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "reservation-events", groupId = "event-service-reservation-group")
    public void handleReservationEvent(String eventPayload) {
        try {
            log.info("→ Received Reservation Event");
            
            ReservationEventMessage event = objectMapper.readValue(eventPayload, ReservationEventMessage.class);
            
            switch (event.eventType) {
                case "CREATED":
                    handleReservationCreated(event);
                    break;
                case "CANCELLED":
                    handleReservationCancelled(event);
                    break;
                case "CONFIRMED":
                    handleReservationConfirmed(event);
                    break;
                default:
                    log.warn("⚠ Unknown reservation event type: {}", event.eventType);
            }
            
            log.info("✓ Reservation event processed successfully");
        } catch (Exception e) {
            log.error("✗ Error processing reservation event: {}", e.getMessage());
            throw new RuntimeException("Failed to process reservation event", e);
        }
    }

    // ==================== USER EVENT HANDLERS ====================

    private void handleUserCreated(UserEventMessage event) {
        log.info("✓ User created: userId={}", event.userId);
        // Can be used to cache user info or create default preferences
    }

    private void handleUserUpdated(UserEventMessage event) {
        log.info("✓ User updated: userId={}", event.userId);
        // Update any cached user data
    }

    private void handleUserDeleted(UserEventMessage event) {
        log.info("✓ User deleted: userId={}", event.userId);
        // Handle cascading deletion or archiving of user's events
    }

    // ==================== FEEDBACK EVENT HANDLERS ====================

    private void handleFeedbackCreated(FeedbackEventMessage event) {
        log.info("✓ Feedback created: eventId={}, rating={}", event.eventId, event.rating);
        // Update event rating or statistics
    }

    private void handleFeedbackModerated(FeedbackEventMessage event) {
        log.info("✓ Feedback moderated: feedbackId={}", event.feedbackId);
        // Update moderation status
    }

    private void handleFeedbackDeleted(FeedbackEventMessage event) {
        log.info("✓ Feedback deleted: feedbackId={}", event.feedbackId);
        // Update event statistics
    }

    // ==================== RESERVATION EVENT HANDLERS ====================

    private void handleReservationCreated(ReservationEventMessage event) {
        log.info("✓ Reservation created: eventId={}, userId={}, places={}", 
                event.eventId, event.userId, event.numberOfPlaces);
        // Decrement available places in event
    }

    private void handleReservationCancelled(ReservationEventMessage event) {
        log.info("✓ Reservation cancelled: reservationId={}", event.reservationId);
        // Increment available places
    }

    private void handleReservationConfirmed(ReservationEventMessage event) {
        log.info("✓ Reservation confirmed: reservationId={}", event.reservationId);
        // Update payment status or availability
    }

    // ==================== EVENT MESSAGE CLASSES ====================

    static class UserEventMessage {
        public String eventType;
        public Long userId;
        public String username;
        public String email;
    }

    static class FeedbackEventMessage {
        public String eventType;
        public Long feedbackId;
        public Long eventId;
        public Long userId;
        public Integer rating;
        public String status;
    }

    static class ReservationEventMessage {
        public String eventType;
        public String reservationId;
        public Long eventId;
        public Long userId;
        public Integer numberOfPlaces;
        public String status;
    }

    private String text(JsonNode n, String key) {
        if (n == null || n.isNull() || !n.has(key)) return null;
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private Long longVal(JsonNode n, String key) {
        if (n == null || n.isNull() || !n.has(key)) return null;
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asLong();
    }
}

