package com.example.sagaorchestrator.service;

import com.example.sagaorchestrator.client.ReservationServiceClient;
import com.example.sagaorchestrator.client.TicketServiceClient;
import com.example.sagaorchestrator.client.UserServiceClient;
import com.example.sagaorchestrator.dto.CancelRequest;
import com.example.sagaorchestrator.dto.ReservationCreateRequest;
import com.example.sagaorchestrator.dto.ReservationSummaryResponse;
import com.example.sagaorchestrator.dto.SagaReserveRequest;
import com.example.sagaorchestrator.dto.SagaReserveResponse;
import com.example.sagaorchestrator.dto.TicketCreateRequest;
import com.example.sagaorchestrator.dto.TicketLinkRequest;
import com.example.sagaorchestrator.dto.TicketResponse;
import com.example.sagaorchestrator.kafka.SagaEventPublisher;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates a distributed reservation: reservation-service (aggregate + Kafka) then ticket-service,
 * with compensation on failure. Event capacity is enforced only by reservation-service (no duplicate
 * {@code reserve-places} on event-service).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SagaReservationService {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    private final UserServiceClient userServiceClient;
    private final ReservationServiceClient reservationServiceClient;
    private final TicketServiceClient ticketServiceClient;
    private final SagaEventPublisher sagaEventPublisher;

    private final Map<String, SagaReserveResponse> idempotency = new ConcurrentHashMap<>();

    public SagaReserveResponse reserve(SagaReserveRequest request) {
        String key = request.getIdempotencyKey();
        if (key != null && !key.isBlank()) {
            return idempotency.compute(key, (k, existing) -> {
                if (existing != null) {
                    return existing;
                }
                return execute(request, k);
            });
        }
        return execute(request, UUID.randomUUID().toString());
    }

    private SagaReserveResponse execute(SagaReserveRequest request, String sagaId) {
        String reservationId = null;
        Long ticketId = null;

        try {
            Long uid = request.getUserId();
            if (uid != null && uid > 0) {
                ResponseEntity<Boolean> existsResp = userServiceClient.existsById(uid);
                Boolean exists = existsResp.getBody();
                if (!existsResp.getStatusCode().is2xxSuccessful() || !Boolean.TRUE.equals(exists)) {
                    SagaReserveResponse resp = fail(sagaId, "User not found or inactive", null, null);
                    sagaEventPublisher.publish(sagaId, "SAGA_FAILED", request, null, null, "USER_INVALID");
                    putIdempotent(request, resp);
                    return resp;
                }
            }

            ReservationCreateRequest reservationReq = new ReservationCreateRequest();
            reservationReq.setUserId(request.getUserId());
            reservationReq.setEventId(request.getEventId());
            reservationReq.setNumberOfPlaces(request.getPlaces());

            ResponseEntity<ReservationSummaryResponse> reservationResp;
            try {
                reservationResp = reservationServiceClient.create(reservationReq);
            } catch (FeignException e) {
                SagaReserveResponse resp = fail(
                        sagaId,
                        "Reservation creation failed: " + feignErrorBody(e),
                        null,
                        null
                );
                sagaEventPublisher.publish(sagaId, "SAGA_FAILED", request, null, null, "RESERVATION: " + e.status());
                putIdempotent(request, resp);
                return resp;
            }

            if (!reservationResp.getStatusCode().is2xxSuccessful() || reservationResp.getBody() == null
                    || reservationResp.getBody().getId() == null) {
                SagaReserveResponse resp = fail(sagaId, "Reservation creation rejected", null, null);
                sagaEventPublisher.publish(sagaId, "SAGA_FAILED", request, null, null, "RESERVATION_REJECTED");
                putIdempotent(request, resp);
                return resp;
            }

            reservationId = reservationResp.getBody().getId();
            ReservationSummaryResponse created = reservationResp.getBody();
            double totalPrice = created.getTotalPrice() != null ? created.getTotalPrice() : 0.0;
            Long resolvedUserId = created.getUserId() != null && created.getUserId() > 0
                    ? created.getUserId()
                    : (request.getUserId() != null && request.getUserId() > 0 ? request.getUserId() : null);

            TicketCreateRequest ticketReq = new TicketCreateRequest();
            ticketReq.setUserId(resolvedUserId);
            ticketReq.setNombreMaxTickets(request.getPlaces());
            ticketReq.setPrix(totalPrice);
            ticketReq.setStatut("DISPONIBLE");
            ticketReq.setTypeTicket("NORMAL");
            ticketReq.setNomClient(request.getNomClient());
            ticketReq.setEmailClient(request.getEmailClient());

            ResponseEntity<TicketResponse> ticketResp;
            try {
                ticketResp = ticketServiceClient.createTicket(request.getEventId(), ticketReq);
            } catch (FeignException e) {
                compensateCancelReservation(reservationId, request);
                SagaReserveResponse resp = fail(sagaId, "Ticket creation failed: " + feignErrorBody(e), reservationId, null);
                sagaEventPublisher.publish(sagaId, "SAGA_FAILED", request, null, reservationId, "TICKET: " + e.status());
                putIdempotent(request, resp);
                return resp;
            }

            if (!ticketResp.getStatusCode().is2xxSuccessful() || ticketResp.getBody() == null
                    || ticketResp.getBody().getIdTicket() == null) {
                compensateCancelReservation(reservationId, request);
                SagaReserveResponse resp = fail(sagaId, "Ticket creation rejected", reservationId, null);
                sagaEventPublisher.publish(sagaId, "SAGA_FAILED", request, null, reservationId, "TICKET_REJECTED");
                putIdempotent(request, resp);
                return resp;
            }
            ticketId = ticketResp.getBody().getIdTicket();

            try {
                reservationServiceClient.attachTicket(reservationId, new TicketLinkRequest(ticketId));
            } catch (FeignException e) {
                try {
                    ticketServiceClient.deleteTicket(ticketId);
                } catch (Exception ex) {
                    log.warn("Could not delete ticket {} after link failure: {}", ticketId, ex.getMessage());
                }
                compensateCancelReservation(reservationId, request);
                SagaReserveResponse resp = fail(
                        sagaId,
                        "Could not link ticket to reservation: " + feignErrorBody(e),
                        reservationId,
                        null
                );
                sagaEventPublisher.publish(sagaId, "SAGA_FAILED", request, null, reservationId, "LINK: " + e.status());
                putIdempotent(request, resp);
                return resp;
            }

            log.info("Saga {} completed reservationId={} ticketId={} amount={}", sagaId, reservationId, ticketId, totalPrice);

            SagaReserveResponse ok = SagaReserveResponse.builder()
                    .sagaId(sagaId)
                    .status(STATUS_COMPLETED)
                    .reservationId(reservationId)
                    .ticketId(ticketId)
                    .message("Reservation saga completed")
                    .build();
            sagaEventPublisher.publish(sagaId, "SAGA_COMPLETED", request, ticketId, reservationId, "OK");
            putIdempotent(request, ok);
            return ok;
        } catch (Exception e) {
            log.error("Saga {} unexpected error", sagaId, e);
            if (reservationId != null) {
                compensateCancelReservation(reservationId, request);
            }
            if (ticketId != null) {
                try {
                    ticketServiceClient.deleteTicket(ticketId);
                } catch (Exception ex) {
                    log.warn("Could not delete ticket {} during compensation: {}", ticketId, ex.getMessage());
                }
            }
            SagaReserveResponse resp = fail(sagaId, e.getMessage(), reservationId, ticketId);
            sagaEventPublisher.publish(sagaId, "SAGA_FAILED", request, ticketId, reservationId, "ERROR: " + e.getMessage());
            putIdempotent(request, resp);
            return resp;
        }
    }

    private void compensateCancelReservation(String reservationId, SagaReserveRequest request) {
        try {
            CancelRequest cancelRequest = new CancelRequest();
            cancelRequest.setReason("Saga compensation: upstream step failed");
            cancelRequest.setCancelledBy(request.getUserId() != null && request.getUserId() > 0 ? request.getUserId() : null);
            reservationServiceClient.cancel(reservationId, cancelRequest);
            log.warn("Saga compensation: cancelled reservationId={}", reservationId);
        } catch (Exception e) {
            log.warn("Reservation cancellation compensation failed: {}", e.getMessage());
        }
    }

    private static SagaReserveResponse fail(String sagaId, String message, String reservationId, Long ticketId) {
        return SagaReserveResponse.builder()
                .sagaId(sagaId)
                .status(STATUS_FAILED)
                .reservationId(reservationId)
                .ticketId(ticketId)
                .message(message)
                .build();
    }

    private void putIdempotent(SagaReserveRequest request, SagaReserveResponse response) {
        String key = request.getIdempotencyKey();
        if (key != null && !key.isBlank()) {
            idempotency.put(key, response);
        }
    }

    private static String feignErrorBody(FeignException e) {
        String raw = e.contentUTF8();
        if (raw == null || raw.isBlank()) {
            return e.getMessage() != null ? e.getMessage() : ("HTTP " + e.status());
        }
        raw = raw.trim();
        return raw.length() > 480 ? raw.substring(0, 480) + "…" : raw;
    }
}
