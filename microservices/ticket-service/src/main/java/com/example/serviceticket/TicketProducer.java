package com.example.serviceticket;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketProducer {

    private static final String TOPIC = "ticket-events";
    private static final String AGGREGATE_TYPE = "Ticket";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendTicketCreated(Ticket ticket) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    "CREATED",
                    AGGREGATE_TYPE,
                    String.valueOf(ticket.getIdTicket()),
                    ticket,
                    null,
                    null,
                    null
            );
            String payload = objectMapper.writeValueAsString(envelope);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, "CREATED")
                    .build();
            kafkaTemplate.send(message);
            log.info("Kafka sent ticket CREATED: id={}", ticket.getIdTicket());
        } catch (Exception e) {
            log.error("Error sending ticket CREATED event", e);
        }
    }

    public void sendTicketUpdated(Ticket ticket) {
        try {
            EventEnvelope envelope = EventEnvelope.v1(
                    "UPDATED",
                    AGGREGATE_TYPE,
                    String.valueOf(ticket.getIdTicket()),
                    ticket,
                    null,
                    null,
                    null
            );
            String payload = objectMapper.writeValueAsString(envelope);
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, "UPDATED")
                    .build();
            kafkaTemplate.send(message);
            log.info("Kafka sent ticket UPDATED: id={}", ticket.getIdTicket());
        } catch (Exception e) {
            log.error("Error sending ticket UPDATED event", e);
        }
    }

    public void sendTicketDeleted(Long id) {
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
            kafkaTemplate.send(message);
            log.info("Kafka sent ticket DELETED: id={}", id);
        } catch (Exception e) {
            log.error("Error sending ticket DELETED event", e);
        }
    }
}
