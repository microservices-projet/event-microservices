package com.example.reservationservice.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.example.eventcontract.PaymentEventPayload;
import com.example.reservationservice.document.ProcessedPaymentEvent;
import com.example.reservationservice.repository.ProcessedPaymentEventRepository;
import com.example.reservationservice.service.ReservationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationConsumer {
    private final ObjectMapper objectMapper;
    private final ReservationService reservationService;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;

    @KafkaListener(topics = "ticket-events", groupId = "reservation-service-group")
    public void consumeTicketEvent(ConsumerRecord<String, String> record,
                                   @Header(value = "event-type", required = false) String eventType) {
        try {
            JsonNode root = objectMapper.readTree(record.value());
            JsonNode n = root.has("payload") ? root.get("payload") : root;
            String effectiveEventType = root.has("eventType") ? root.get("eventType").asText() : eventType;
            String aggregateId = root.has("aggregateId") ? root.get("aggregateId").asText() : null;

            log.info("Received ticket event: type={}, aggregateId={}, payload={}", effectiveEventType, aggregateId, n);
        } catch (Exception e) {
            log.info("Received ticket event (non-envelope): type={}, payload={}", eventType, record.value());
        }
    }

    @KafkaListener(topics = EventEnvelopeContract.PAYMENT_EVENTS_TOPIC, groupId = "reservation-service-payment-group")
    public void consumePaymentEvent(
            String payload,
            @Header(value = EventEnvelopeContract.EVENT_TYPE_HEADER, required = false) String eventTypeHeader
    ) {
        try {
            EventEnvelope envelope = objectMapper.readValue(payload, EventEnvelope.class);
            if (envelope.getEventId() != null && processedPaymentEventRepository.existsById(envelope.getEventId())) {
                return;
            }
            PaymentEventPayload paymentPayload = objectMapper.convertValue(envelope.getPayload(), PaymentEventPayload.class);
            String eventType = envelope.getEventType() != null ? envelope.getEventType() : eventTypeHeader;
            String reservationId = paymentPayload.getReservationId();
            if (reservationId == null || reservationId.isBlank()) {
                return;
            }

            if (EventEnvelopeContract.EVENT_PAYMENT_INITIATED.equals(eventType) || "PAYMENT_INITIATED".equals(eventType)) {
                reservationService.resolvePaymentInitiated(reservationId, paymentPayload.getPaymentId(), paymentPayload.getSessionId());
            } else if (EventEnvelopeContract.EVENT_PAYMENT_COMPLETED.equals(eventType) || "PAYMENT_COMPLETED".equals(eventType)) {
                reservationService.resolvePaymentCompleted(reservationId, paymentPayload.getPaymentId());
            } else if (EventEnvelopeContract.EVENT_PAYMENT_FAILED.equals(eventType) || "PAYMENT_FAILED".equals(eventType)) {
                reservationService.resolvePaymentFailed(reservationId, paymentPayload.getFailureReason());
            } else {
                return;
            }

            if (envelope.getEventId() != null) {
                processedPaymentEventRepository.save(new ProcessedPaymentEvent(envelope.getEventId(), LocalDateTime.now()));
            }
        } catch (Exception e) {
            log.warn("Could not process payment event: {}", e.getMessage());
        }
    }
}
