package org.example.eventmodule.outbox;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventOutboxPublisher {

    private final OutboxEventRecordRepository outboxRepo;
    private final DomainEventRecordRepository domainRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.outbox.max-attempts:5}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        var batch = outboxRepo.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if (batch.isEmpty()) return;

        for (OutboxEventRecord outbox : batch) {
            try {
                publishOne(outbox);
                outbox.setStatus(OutboxStatus.SENT);
                outbox.setSentAt(Instant.now());
                outboxRepo.save(outbox);
            } catch (Exception e) {
                int nextAttempts = outbox.getAttempts() + 1;
                outbox.setAttempts(nextAttempts);
                outbox.setLastError(e.getMessage());
                if (nextAttempts >= maxAttempts) {
                    outbox.setStatus(OutboxStatus.FAILED);
                }
                outboxRepo.save(outbox);
                log.warn("Outbox publish failed (domainEventId={} attempt={}): {}", outbox.getDomainEventId(), nextAttempts, e.getMessage());
            }
        }
    }

    private void publishOne(OutboxEventRecord outbox) throws Exception {
        DomainEventRecord domain = domainRepo.findByEventId(outbox.getDomainEventId())
                .orElseThrow(() -> new IllegalStateException("Domain event not found: " + outbox.getDomainEventId()));

        JsonNode payloadNode = objectMapper.readTree(domain.getPayloadJson());
        EventEnvelope envelope = new EventEnvelope();
        envelope.setSchemaVersion(EventEnvelopeContract.SCHEMA_VERSION_V1);
        envelope.setEventId(domain.getEventId());
        envelope.setAggregateType(domain.getAggregateType());
        envelope.setAggregateId(domain.getAggregateId());
        envelope.setEventType(domain.getEventType());
        envelope.setOccurredAt(domain.getOccurredAt());
        envelope.setCorrelationId(domain.getCorrelationId());
        envelope.setCausationId(domain.getCausationId());
        envelope.setActor(domain.getActor());
        envelope.setPayload(payloadNode);

        String envelopeJson = objectMapper.writeValueAsString(envelope);

        Message<String> message = MessageBuilder
                .withPayload(envelopeJson)
                .setHeader(KafkaHeaders.TOPIC, outbox.getKafkaTopic())
                .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, domain.getEventType())
                .build();

        kafkaTemplate.send(message).get(5, TimeUnit.SECONDS);
    }
}

