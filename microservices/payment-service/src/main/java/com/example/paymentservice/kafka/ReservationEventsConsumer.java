package com.example.paymentservice.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.example.eventcontract.ReservationCreatedPayload;
import com.example.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventsConsumer {
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    @KafkaListener(topics = EventEnvelopeContract.RESERVATION_EVENTS_TOPIC, groupId = "payment-service-group")
    public void onReservationEvent(
            String payload,
            @Header(value = EventEnvelopeContract.EVENT_TYPE_HEADER, required = false) String eventTypeHeader,
            @Header(value = EventEnvelopeContract.CORRELATION_ID_HEADER, required = false) String correlationIdHeader
    ) {
        try {
            EventEnvelope envelope = objectMapper.readValue(payload, EventEnvelope.class);
            String eventType = envelope.getEventType() != null ? envelope.getEventType() : eventTypeHeader;
            if (!EventEnvelopeContract.EVENT_RESERVATION_CREATED.equals(eventType)
                    && !"CREATED".equals(eventType)) {
                return;
            }

            ReservationCreatedPayload created = objectMapper.convertValue(envelope.getPayload(), ReservationCreatedPayload.class);
            String reservationId = created.getReservationId() != null ? created.getReservationId() : envelope.getAggregateId();
            String correlationId = envelope.getCorrelationId() != null ? envelope.getCorrelationId() : correlationIdHeader;
            paymentService.handleReservationCreated(
                    envelope.getEventId(),
                    reservationId,
                    created.getTotalPrice(),
                    correlationId
            );
        } catch (Exception e) {
            log.warn("Could not process reservation event: {}", e.getMessage());
        }
    }
}
