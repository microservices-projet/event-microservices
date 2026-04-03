package com.example.reservationservice.controller;

import com.example.reservationservice.client.UserClient;
import com.example.reservationservice.dto.*;
import com.example.reservationservice.exception.ReservationException;
import com.example.reservationservice.service.ReservationService;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        // Frontend Keycloak hydrate souvent id=0 : resoudre l'id via le JWT relaye vers user-service.
        if (request.getUserId() == null || request.getUserId() == 0L) {
            try {
                request.setUserId(userClient.getCurrentProfile().getId());
            } catch (FeignException e) {
                if (e.status() == 404) {
                    throw new ReservationException(
                            "Compte local requis : inscrivez-vous avec le meme nom d'utilisateur que Keycloak pour reserver.");
                }
                throw e;
            }
        }
        return ResponseEntity.ok(reservationService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAll() {
        return ResponseEntity.ok(reservationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getByUserId(userId));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ReservationResponse>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(reservationService.getByEventId(eventId));
    }

    @GetMapping("/availability/{eventId}")
    public ResponseEntity<AvailabilityResponse> checkAvailability(@PathVariable Long eventId) {
        return ResponseEntity.ok(reservationService.checkAvailability(eventId));
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> confirm(@PathVariable String id) {
        return ResponseEntity.ok(reservationService.confirm(id));
    }

    @PatchMapping("/{id}/ticket")
    public ResponseEntity<ReservationResponse> attachTicket(
            @PathVariable String id,
            @Valid @RequestBody TicketLinkRequest linkRequest) {
        Long userId = userClient.getCurrentProfile().getId();
        return ResponseEntity.ok(reservationService.attachTicket(id, linkRequest, userId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @PathVariable String id,
            @RequestBody CancelRequest request) {
        return ResponseEntity.ok(reservationService.cancel(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        reservationService.delete(id);
        return ResponseEntity.ok("Réservation supprimée");
    }
}
