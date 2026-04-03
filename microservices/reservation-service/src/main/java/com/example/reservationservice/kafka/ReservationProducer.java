package com.example.reservationservice.kafka;

import com.example.eventcontract.EventEnvelope;
import com.example.eventcontract.EventEnvelopeContract;
import com.example.reservationservice.document.Reservation;
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
public class ReservationProducer {

    private static final String TOPIC = "reservation-events";
    private static final String AGGREGATE_TYPE = "Reservation";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendReservationCreated(Reservation reservation) {
        sendMessage(reservation, "CREATED");
    }

    public void sendReservationConfirmed(Reservation reservation) {
        sendMessage(reservation, "CONFIRMED");
    }

    public void sendReservationCancelled(Reservation reservation) {
        sendMessage(reservation, "CANCELLED");
    }

    private void sendMessage(Reservation reservation, String eventType) {
        String correlationId = "reservation-" + reservation.getId();
        EventEnvelope envelope = EventEnvelope.v1(
                eventType,
                AGGREGATE_TYPE,
                String.valueOf(reservation.getId()),
                reservation,
                correlationId,
                null,
                null
        );
        Message<EventEnvelope> message = MessageBuilder
                .withPayload(envelope)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(EventEnvelopeContract.EVENT_TYPE_HEADER, eventType)
                .setHeader(EventEnvelopeContract.CORRELATION_ID_HEADER, correlationId)
                .build();
        kafkaTemplate.send(message);
        log.info("Kafka reservation {} id={}", eventType, reservation.getId());
    }
}
