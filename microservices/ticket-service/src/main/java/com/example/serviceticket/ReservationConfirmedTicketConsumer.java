package com.example.serviceticket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationConfirmedTicketConsumer {

    private static final String TOPIC = "reservation-events";

    private final ObjectMapper objectMapper;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final UserInternalClient userInternalClient;

    @KafkaListener(topics = TOPIC, groupId = "ticket-service-reservation-saga")
    public void onReservationConfirmed(
            String payload,
            @Header(value = "event-type", required = false) String eventTypeHeader
    ) {
        try {
            JsonNode envelope = objectMapper.readTree(payload);
            String eventType = envelope.hasNonNull("eventType") ? envelope.get("eventType").asText() : eventTypeHeader;
            if (!"CONFIRMED".equals(eventType)) return;

            JsonNode reservation = envelope.get("payload");
            if (reservation == null) return;

            Long userId = reservation.hasNonNull("userId") ? reservation.get("userId").asLong() : null;
            Long eventId = reservation.hasNonNull("eventId") ? reservation.get("eventId").asLong() : null;
            Integer numberOfPlaces = reservation.hasNonNull("numberOfPlaces") ? reservation.get("numberOfPlaces").asInt() : null;
            Double totalPrice = reservation.hasNonNull("totalPrice") ? reservation.get("totalPrice").asDouble() : null;

            if (userId == null || eventId == null) {
                log.warn("Skipping ticket creation due to missing reservation fields (userId={}, eventId={})", userId, eventId);
                return;
            }

            // Idempotency: skip if a ticket already exists for (eventId, userId).
            List<Ticket> existingTickets = ticketRepository.findByEventId(eventId);
            boolean alreadyExists = existingTickets.stream()
                    .anyMatch(t -> t.getUserId() != null && t.getUserId().equals(userId));
            if (alreadyExists) {
                log.info("Ticket already exists for reservation confirmation (eventId={}, userId={}), skipping.",
                        eventId, userId);
                return;
            }

            UserDTO user = userInternalClient.getUserById(userId);

            Double pricePerTicket = null;
            if (totalPrice != null && numberOfPlaces != null && numberOfPlaces > 0) {
                pricePerTicket = totalPrice / numberOfPlaces;
            }

            Ticket ticket = Ticket.builder()
                    .userId(userId)
                    .nomClient(user.getUsername())
                    .emailClient(user.getEmail())
                    .nombreMaxTickets(numberOfPlaces != null ? numberOfPlaces : 1)
                    .prix(pricePerTicket != null ? pricePerTicket : totalPrice)
                    .statut(StatusTicket.DISPONIBLE)
                    .typeTicket(TypeTicket.NORMAL)
                    .build();

            ticketService.createTicket(ticket, eventId);
            log.info("Created ticket from reservation confirmed (eventId={}, userId={})", eventId, userId);
        } catch (Exception e) {
            log.error("Ticket creation from reservation failed", e);
            throw new RuntimeException(e);
        }
    }
}

