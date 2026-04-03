package com.example.paymentservice.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.example.eventcontract.PaymentEventPayload;
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
public class PaymentEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(PaymentEventPayload payload, String eventType, String correlationId) {
        EventEnvelope envelope = EventEnvelope.v1(
                eventType,
                "Payment",
                payload.getPaymentId(),
                payload,
                correlationId,
                null,
                "payment-service"
        );
        Message<EventEnvelope> message = MessageBuilder
                .withPayload(envelope)
                .setHeader(KafkaHeaders.TOPIC, EventEnvelopeContract.PAYMENT_EVENTS_TOPIC)
                .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, eventType)
                .setHeader(EventEnvelopeContract.CORRELATION_ID_HEADER, correlationId)
                .build();
        kafkaTemplate.send(message);
        log.info("Kafka payment {} paymentId={} reservationId={}", eventType, payload.getPaymentId(), payload.getReservationId());
    }
}
