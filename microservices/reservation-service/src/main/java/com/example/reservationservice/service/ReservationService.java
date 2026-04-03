package com.example.reservationservice.service;

import com.example.reservationservice.client.EventClient;
import com.example.reservationservice.client.TicketClient;
import com.example.reservationservice.client.UserClient;
import com.example.reservationservice.document.PaymentStatus;
import com.example.reservationservice.document.Reservation;
import com.example.reservationservice.document.ReservationStatus;
import com.example.reservationservice.dto.*;
import com.example.reservationservice.exception.ReservationException;
import com.example.reservationservice.exception.ReservationNotFoundException;
import com.example.reservationservice.kafka.ReservationProducer;
import com.example.reservationservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationProducer reservationProducer;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final TicketClient ticketClient;

    public ReservationResponse create(ReservationRequest request) {
        EventDTO event;
        try {
            event = eventClient.getEventById(request.getEventId());
        } catch (Exception e) {
            throw new ReservationException("Evenement introuvable : " + request.getEventId());
        }

        AvailabilityResponse availability = checkAvailability(request.getEventId());
        if (availability.getAvailablePlaces() < request.getNumberOfPlaces()) {
            throw new ReservationException(
                    String.format("Places insuffisantes. Disponible: %d, Demande: %d",
                            availability.getAvailablePlaces(), request.getNumberOfPlaces()));
        }

        double totalPrice = (event.getPrice() != null ? event.getPrice() : 0) * request.getNumberOfPlaces();

        Reservation reservation = Reservation.builder()
                .userId(request.getUserId())
                .eventId(request.getEventId())
                .numberOfPlaces(request.getNumberOfPlaces())
                .totalPrice(totalPrice)
                .reservationDate(LocalDateTime.now())
                .build();

        reservation.addAuditEntry("CREATED", request.getUserId(),
                "Reservation de " + request.getNumberOfPlaces() + " places");

        Reservation saved = reservationRepository.save(reservation);
        trySendKafka(() -> reservationProducer.sendReservationCreated(saved));
        return mapToResponse(saved);
    }

    public List<ReservationResponse> getAll() {
        return reservationRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public ReservationResponse getById(String id) {
        return mapToResponse(findOrThrow(id));
    }

    public List<ReservationResponse> getByUserId(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ReservationResponse> getByEventId(Long eventId) {
        return reservationRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public AvailabilityResponse checkAvailability(Long eventId) {
        int totalPlaces = 0;
        try {
            EventDTO event = eventClient.getEventById(eventId);
            totalPlaces = event.getNbPlaces() != null ? event.getNbPlaces() : 0;
        } catch (Exception e) {
            log.warn("Could not fetch event {}: {}", eventId, e.getMessage());
        }

        List<Reservation> activeReservations = reservationRepository.findByEventIdAndStatusIn(
                eventId, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));
        int reservedPlaces = activeReservations.stream()
                .mapToInt(Reservation::getNumberOfPlaces).sum();

        return AvailabilityResponse.builder()
                .eventId(eventId)
                .totalPlaces(totalPlaces)
                .reservedPlaces(reservedPlaces)
                .availablePlaces(Math.max(0, totalPlaces - reservedPlaces))
                .build();
    }

    public ReservationResponse confirm(String id) {
        Reservation reservation = findOrThrow(id);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ReservationException("Seules les reservations en attente peuvent etre confirmees");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setPaymentStatus(PaymentStatus.PAID);
        reservation.setConfirmationDate(LocalDateTime.now());
        reservation.addAuditEntry("CONFIRMED", reservation.getUserId(), "Reservation confirmee");

        Reservation saved = reservationRepository.save(reservation);
        trySendKafka(() -> reservationProducer.sendReservationConfirmed(saved));
        return mapToResponse(saved);
    }

    /**
     * Attaches a ticket created by the saga (or another service) to this reservation.
     */
    public ReservationResponse attachTicket(String id, TicketLinkRequest linkRequest, Long currentUserId) {
        Reservation reservation = findOrThrow(id);
        if (!reservation.getUserId().equals(currentUserId)) {
            throw new ReservationException("Operation non autorisee pour cette reservation");
        }
        if (reservation.getTicketId() != null) {
            return mapToResponse(reservation);
        }
        if (linkRequest.getTicketId() == null) {
            throw new ReservationException("ticketId requis");
        }
        reservation.setTicketId(linkRequest.getTicketId());
        reservation.addAuditEntry(
                "TICKET_ATTACHED",
                currentUserId,
                "Billet lie: " + linkRequest.getTicketId()
        );
        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    public ReservationResponse cancel(String id, CancelRequest request) {
        Reservation reservation = findOrThrow(id);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ReservationException("Reservation deja annulee");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancellationDate(LocalDateTime.now());
        reservation.setCancellationReason(request.getReason());
        if (reservation.getPaymentStatus() == PaymentStatus.PAID) {
            reservation.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        Long cancelledBy = request.getCancelledBy() != null ? request.getCancelledBy() : reservation.getUserId();
        reservation.addAuditEntry("CANCELLED", cancelledBy,
                "Annulee: " + (request.getReason() != null ? request.getReason() : "Aucune raison"));

        Reservation saved = reservationRepository.save(reservation);
        trySendKafka(() -> reservationProducer.sendReservationCancelled(saved));
        return mapToResponse(saved);
    }

    public ReservationResponse resolvePaymentCompleted(String reservationId, String paymentId) {
        Reservation reservation = findOrThrow(reservationId);
        if (reservation.getStatus() == ReservationStatus.CONFIRMED && reservation.getPaymentStatus() == PaymentStatus.PAID) {
            return mapToResponse(reservation);
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return mapToResponse(reservation);
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setPaymentStatus(PaymentStatus.PAID);
        reservation.setConfirmationDate(LocalDateTime.now());
        reservation.addAuditEntry("PAYMENT_COMPLETED", reservation.getUserId(), "Paiement confirme: " + paymentId);
        Reservation saved = reservationRepository.save(reservation);
        trySendKafka(() -> reservationProducer.sendReservationConfirmed(saved));
        return mapToResponse(saved);
    }

    public ReservationResponse resolvePaymentInitiated(String reservationId, String paymentId, String sessionId) {
        Reservation reservation = findOrThrow(reservationId);
        if (reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return mapToResponse(reservation);
        }
        reservation.setPaymentStatus(PaymentStatus.PENDING);
        reservation.addAuditEntry(
                "PAYMENT_INITIATED",
                reservation.getUserId(),
                "Paiement initialise: " + paymentId + (sessionId != null ? " (session " + sessionId + ")" : "")
        );
        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    public ReservationResponse resolvePaymentFailed(String reservationId, String reason) {
        Reservation reservation = findOrThrow(reservationId);
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return mapToResponse(reservation);
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.FAILED) {
            reservation.setPaymentStatus(PaymentStatus.FAILED);
            Reservation saved = reservationRepository.save(reservation);
            return mapToResponse(saved);
        }

        reservation.setStatus(ReservationStatus.FAILED);
        reservation.setPaymentStatus(PaymentStatus.FAILED);
        reservation.addAuditEntry("PAYMENT_FAILED", reservation.getUserId(),
                "Paiement echoue" + (reason != null && !reason.isBlank() ? ": " + reason : ""));
        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    public void delete(String id) {
        if (!reservationRepository.existsById(id)) {
            throw new ReservationNotFoundException("Reservation non trouvee : " + id);
        }
        reservationRepository.deleteById(id);
    }

    private Reservation findOrThrow(String id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation non trouvee : " + id));
    }

    private void trySendKafka(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.warn("Kafka unavailable, operation saved without notification: {}", e.getMessage());
        }
    }

    private ReservationResponse mapToResponse(Reservation r) {
        ReservationResponse.ReservationResponseBuilder builder = ReservationResponse.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .eventId(r.getEventId())
                .ticketId(r.getTicketId())
                .numberOfPlaces(r.getNumberOfPlaces())
                .totalPrice(r.getTotalPrice())
                .status(r.getStatus())
                .paymentStatus(r.getPaymentStatus())
                .reservationDate(r.getReservationDate())
                .confirmationDate(r.getConfirmationDate())
                .cancellationDate(r.getCancellationDate())
                .cancellationReason(r.getCancellationReason())
                .auditLog(r.getAuditLog())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt());

        try {
            UserDTO user = userClient.getUserById(r.getUserId());
            builder.username(user.getUsername());
        } catch (Exception e) {
            log.warn("Could not fetch user {}: {}", r.getUserId(), e.getMessage());
        }

        try {
            EventDTO event = eventClient.getEventById(r.getEventId());
            builder.eventTitle(event.getTitle());
            builder.eventDate(event.getDate());
        } catch (Exception e) {
            log.warn("Could not fetch event {}: {}", r.getEventId(), e.getMessage());
        }

        return builder.build();
    }
}
