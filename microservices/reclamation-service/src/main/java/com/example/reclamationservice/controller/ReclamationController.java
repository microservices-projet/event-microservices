package com.example.reclamationservice.controller;

import com.example.reclamationservice.dto.*;
import com.example.reclamationservice.entity.ReclamationStatus;
import com.example.reclamationservice.service.ReclamationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReclamationController {

    private final ReclamationService reclamationService;

    @PostMapping
    public ResponseEntity<ReclamationResponse> create(@Valid @RequestBody ReclamationRequest request) {
        return ResponseEntity.ok(reclamationService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ReclamationResponse>> getAll() {
        return ResponseEntity.ok(reclamationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReclamationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reclamationService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReclamationResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reclamationService.getByUserId(userId));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ReclamationResponse>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(reclamationService.getByEventId(eventId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReclamationResponse>> getByStatus(@PathVariable ReclamationStatus status) {
        return ResponseEntity.ok(reclamationService.getByStatus(status));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ReclamationResponse> assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignRequest request) {
        return ResponseEntity.ok(reclamationService.assign(id, request));
    }

    @PatchMapping("/{id}/respond")
    public ResponseEntity<ReclamationResponse> respond(
            @PathVariable Long id,
            @Valid @RequestBody RespondRequest request) {
        return ResponseEntity.ok(reclamationService.respond(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReclamationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(reclamationService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        reclamationService.delete(id);
        return ResponseEntity.ok("Réclamation supprimée");
    }
}
