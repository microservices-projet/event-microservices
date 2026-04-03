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

@Component
@RequiredArgsConstructor
@Slf4j
public class EventKafkaProducer {
    private static final String TOPIC = "event-events";
    private static final String AGGREGATE_TYPE = "Event";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendCreated(Event event) {
        send(event, "CREATED");
    }

    public void sendUpdated(Event event) {
        send(event, "UPDATED");
    }

    public void sendDeleted(Event event) {
        send(event, "DELETED");
    }

    private void send(Event event, String eventType) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    eventType,
                    AGGREGATE_TYPE,
                    String.valueOf(event.getId()),
                    event,
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
            kafkaTemplate.send(message);
            log.info("Kafka event {} id={}", eventType, event.getId());
        } catch (Exception e) {
            log.warn("Kafka unavailable, failed to publish {} for event {}: {}", eventType, event.getId(), e.getMessage());
        }
    }
}
