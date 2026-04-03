package com.example.reclamationservice.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.example.reclamationservice.entity.Reclamation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReclamationProducer {

    private static final String TOPIC = "reclamation-events";
    private static final String AGGREGATE_TYPE = "Reclamation";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendReclamationCreated(Reclamation reclamation) {
        sendMessage(reclamation, "CREATED");
    }

    public void sendReclamationUpdated(Reclamation reclamation) {
        sendMessage(reclamation, "UPDATED");
    }

    public void sendReclamationDeleted(Long id) {
        EventEnvelope envelope = EventEnvelope.v1(
                "DELETED",
                AGGREGATE_TYPE,
                String.valueOf(id),
                id,
                null,
                null,
                null
        );
        Message<EventEnvelope> message = MessageBuilder
                .withPayload(envelope)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, "DELETED")
                .build();
        kafkaTemplate.send(message);
        log.info("Kafka reclamation DELETED id={}", id);
    }

    private void sendMessage(Reclamation reclamation, String eventType) {
        EventEnvelope envelope = EventEnvelope.v1(
                eventType,
                AGGREGATE_TYPE,
                String.valueOf(reclamation.getId()),
                reclamation,
                null,
                null,
                null
        );
        Message<EventEnvelope> message = MessageBuilder
                .withPayload(envelope)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, eventType)
                .build();
        kafkaTemplate.send(message);
        log.info("Kafka reclamation {} id={}", eventType, reclamation.getId());
    }
}
