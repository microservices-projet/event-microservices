package com.saladin.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saladin.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {
    private static final String TOPIC = "user-events";
    private static final String AGGREGATE_TYPE = "User";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendUserCreated(User user) {
        send(user, "CREATED");
    }

    public void sendUserUpdated(User user) {
        send(user, "UPDATED");
    }

    public void sendUserDeleted(Long id) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    "DELETED",
                    AGGREGATE_TYPE,
                    String.valueOf(id),
                    id,
                    null,
                    null,
                    null
            );
            String payload = objectMapper.writeValueAsString(envelope);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, "DELETED")
                    .build();
            sendAsync(message, "DELETED", String.valueOf(id));
        } catch (Exception e) {
            log.error("Error preparing user DELETED event", e);
        }
    }

    /**
     * Publishes off the calling thread so HTTP and startup paths are not blocked on broker/metadata.
     * Failures are logged only; user flows stay available if Kafka is down.
     */
    private void sendAsync(Message<String> message, String eventType, String aggregateLabel) {
        CompletableFuture.runAsync(() ->
                kafkaTemplate.send(message).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Kafka user {} not delivered for {} (is the broker running?): {}",
                                eventType, aggregateLabel, ex.toString());
                    } else {
                        log.info("Kafka user {} {}", eventType, aggregateLabel);
                    }
                })
        );
    }

    private void send(User user, String eventType) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    eventType,
                    AGGREGATE_TYPE,
                    String.valueOf(user.getId()),
                    user,
                    null,
                    null,
                    null
            );
            String payload = objectMapper.writeValueAsString(envelope);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, eventType)
                    .build();
            sendAsync(message, eventType, "id=" + user.getId());
        } catch (Exception e) {
            log.error("Error preparing user {} event", eventType, e);
        }
    }
}
