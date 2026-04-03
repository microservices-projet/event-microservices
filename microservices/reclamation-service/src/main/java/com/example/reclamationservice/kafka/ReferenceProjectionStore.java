package com.example.reclamationservice.kafka;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ReferenceProjectionStore {

    private final Map<Long, String> usernamesByUserId = new ConcurrentHashMap<>();
    private final Map<Long, String> eventTitlesByEventId = new ConcurrentHashMap<>();
    private final Map<Long, String> ticketLabelsByTicketId = new ConcurrentHashMap<>();
    private final Map<String, String> reservationLabelsByReservationId = new ConcurrentHashMap<>();

    public Optional<String> username(Long userId) {
        if (userId == null) return Optional.empty();
        return Optional.ofNullable(usernamesByUserId.get(userId));
    }

    public Optional<String> eventTitle(Long eventId) {
        if (eventId == null) return Optional.empty();
        return Optional.ofNullable(eventTitlesByEventId.get(eventId));
    }

    public Optional<String> ticketLabel(Long ticketId) {
        if (ticketId == null) return Optional.empty();
        return Optional.ofNullable(ticketLabelsByTicketId.get(ticketId));
    }

    public Optional<String> reservationLabel(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) return Optional.empty();
        return Optional.ofNullable(reservationLabelsByReservationId.get(reservationId));
    }

    public void upsertUsername(Long userId, String username) {
        if (userId != null && username != null && !username.isBlank()) {
            usernamesByUserId.put(userId, username);
        }
    }

    public void removeUser(Long userId) {
        if (userId != null) {
            usernamesByUserId.remove(userId);
        }
    }

    public void upsertEventTitle(Long eventId, String title) {
        if (eventId != null && title != null && !title.isBlank()) {
            eventTitlesByEventId.put(eventId, title);
        }
    }

    public void removeEvent(Long eventId) {
        if (eventId != null) {
            eventTitlesByEventId.remove(eventId);
        }
    }

    public void upsertTicketLabel(Long ticketId, String label) {
        if (ticketId != null && label != null && !label.isBlank()) {
            ticketLabelsByTicketId.put(ticketId, label);
        }
    }

    public void removeTicket(Long ticketId) {
        if (ticketId != null) {
            ticketLabelsByTicketId.remove(ticketId);
        }
    }

    public void upsertReservationLabel(String reservationId, String label) {
        if (reservationId != null && !reservationId.isBlank() && label != null && !label.isBlank()) {
            reservationLabelsByReservationId.put(reservationId, label);
        }
    }

    public void removeReservation(String reservationId) {
        if (reservationId != null && !reservationId.isBlank()) {
            reservationLabelsByReservationId.remove(reservationId);
        }
    }
}
