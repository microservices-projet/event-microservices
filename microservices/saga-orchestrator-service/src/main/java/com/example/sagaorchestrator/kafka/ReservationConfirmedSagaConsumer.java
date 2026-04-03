package com.example.sagaorchestrator.kafka;

import com.example.sagaorchestrator.client.ReservationServiceClient;
import com.example.sagaorchestrator.client.TicketServiceClient;
import com.example.sagaorchestrator.dto.CancelRequest;
import com.example.sagaorchestrator.dto.TicketCreateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationConfirmedSagaConsumer {

    private final ObjectMapper objectMapper;
    private final TicketServiceClient ticketServiceClient;
    private final ReservationServiceClient reservationServiceClient;

    // MVP idempotency for saga steps (per kafka event id).
    private final Set<String> processedKafkaEventIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "reservation-events", groupId = "saga-orchestrator-group")
    public void onReservationEvent(
            String payload,
            @Header(value = "event-type", required = false) String eventTypeHeader
    ) {
        try {
            JsonNode envelope = objectMapper.readTree(payload);
            String kafkaEventId = envelope.hasNonNull("eventId") ? envelope.get("eventId").asText() : null;
            if (kafkaEventId != null && processedKafkaEventIds.contains(kafkaEventId)) {
                return;
            }

            String eventType = envelope.hasNonNull("eventType") ? envelope.get("eventType").asText() : eventTypeHeader;
            if (!"CONFIRMED".equals(eventType)) return;

            JsonNode reservation = envelope.get("payload");
            String reservationId = reservation != null && reservation.hasNonNull("id") ? reservation.get("id").asText() : null;
            Long userId = reservation != null && reservation.hasNonNull("userId") ? reservation.get("userId").asLong() : null;
            Long eventId = reservation != null && reservation.hasNonNull("eventId") ? reservation.get("eventId").asLong() : null;
            Integer numberOfPlaces = reservation != null && reservation.hasNonNull("numberOfPlaces")
                    ? reservation.get("numberOfPlaces").asInt()
                    : null;
            Double totalPrice = reservation != null && reservation.hasNonNull("totalPrice")
                    ? reservation.get("totalPrice").asDouble()
                    : null;

            if (reservationId == null || userId == null || eventId == null) {
                log.warn("Skipping saga step due to missing reservation fields. reservationId={}, userId={}, eventId={}",
                        reservationId, userId, eventId);
                return;
            }

            if (reservation != null && reservation.hasNonNull("ticketId") && !reservation.get("ticketId").isNull()) {
                log.debug("Reservation {} already has ticketId; skipping duplicate ticket creation", reservationId);
                if (kafkaEventId != null) {
                    processedKafkaEventIds.add(kafkaEventId);
                }
                return;
            }

            TicketCreateRequest ticketRequest = new TicketCreateRequest();
            ticketRequest.setUserId(userId);
            ticketRequest.setNombreMaxTickets(numberOfPlaces != null ? numberOfPlaces : 1);
            ticketRequest.setPrix(totalPrice);
            ticketRequest.setStatut("DISPONIBLE");
            ticketRequest.setTypeTicket("NORMAL");

            log.info("Saga: Reservation CONFIRMED -> creating ticket (reservationId={}, userId={}, eventId={})",
                    reservationId, userId, eventId);

            ticketServiceClient.createTicket(eventId, ticketRequest);
            if (kafkaEventId != null) processedKafkaEventIds.add(kafkaEventId);
        } catch (Exception e) {
            // Compensation: cancel reservation when ticket creation fails.
            try {
                JsonNode envelope = objectMapper.readTree(payload);
                JsonNode reservation = envelope.get("payload");
                String reservationId = reservation != null && reservation.hasNonNull("id") ? reservation.get("id").asText() : null;
                Long userId = reservation != null && reservation.hasNonNull("userId") ? reservation.get("userId").asLong() : null;

                if (reservationId != null) {
                    CancelRequest cancelRequest = new CancelRequest();
                    cancelRequest.setReason("Ticket creation failed");
                    cancelRequest.setCancelledBy(userId);
                    reservationServiceClient.cancel(reservationId, cancelRequest);
                    log.warn("Saga compensation executed: cancelled reservationId={}", reservationId);
                }
            } catch (Exception compensationEx) {
                log.error("Saga compensation failed", compensationEx);
            }

            log.error("Saga failed (ticket creation step)", e);
        }
    }
}

