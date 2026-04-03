package org.example.eventmodule.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eventmodule.Event;
import org.example.eventmodule.EventStatus;
import org.example.eventmodule.request.EventRequest;
import org.example.eventmodule.service.OptimizedEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Optimized Event Controller
 * RESTful API with Feign + Kafka integration for all CRUD operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class OptimizedEventController {

    private final OptimizedEventService eventService;

    // ==================== CREATE ====================

    /**
     * POST /api/v1/events
     * Create new event
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("→ POST /api/v1/events - Create event: {}", request.getTitle());
        Event event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    // ==================== READ ====================

    /**
     * GET /api/v1/events
     * Get all events
     */
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        log.info("→ GET /api/v1/events - Fetch all events");
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/v1/events/{id}
     * Get event by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        log.info("→ GET /api/v1/events/{} - Fetch event", id);
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    /**
     * GET /api/v1/events/organizer/{organizerId}
     * Get events by organizer
     */
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<Event>> getEventsByOrganizer(@PathVariable Long organizerId) {
        log.info("→ GET /api/v1/events/organizer/{} - Fetch events", organizerId);
        List<Event> events = eventService.getEventsByOrganizer(organizerId);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/v1/events/search
     * Search events by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String keyword) {
        log.info("→ GET /api/v1/events/search?keyword={} - Search events", keyword);
        List<Event> events = eventService.searchEvents(keyword);
        return ResponseEntity.ok(events);
    }

    // ==================== UPDATE ====================

    /**
     * PUT /api/v1/events/{id}
     * Update event
     */
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        log.info("→ PUT /api/v1/events/{} - Update event", id);
        Event event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(event);
    }

    /**
     * PATCH /api/v1/events/{id}/status
     * Update event status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Event> updateEventStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status) {
        log.info("→ PATCH /api/v1/events/{}/status?status={} - Update status", id, status);
        Event event = eventService.updateEventStatus(id, status);
        return ResponseEntity.ok(event);
    }

    /**
     * PATCH /api/v1/events/{id}/publish
     * Publish event
     */
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Event> publishEvent(@PathVariable Long id) {
        log.info("→ PATCH /api/v1/events/{}/publish - Publish event", id);
        Event event = eventService.publishEvent(id);
        return ResponseEntity.ok(event);
    }

    // ==================== DELETE ====================

    /**
     * DELETE /api/v1/events/{id}
     * Archive/Delete event
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        log.info("→ DELETE /api/v1/events/{} - Delete event", id);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ACTIONS ====================

    /**
     * POST /api/v1/events/{id}/like
     * Like event
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Event> likeEvent(
            @PathVariable Long id,
            @RequestParam Long userId) {
        log.info("→ POST /api/v1/events/{}/like?userId={} - Like event", id, userId);
        Event event = eventService.likeEvent(id, userId);
        return ResponseEntity.ok(event);
    }

    // ==================== ERROR HANDLING ====================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("✗ Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("✗ Unexpected error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error: " + e.getMessage()));
    }

    static class ErrorResponse {
        public String message;

        ErrorResponse(String message) {
            this.message = message;
        }
    }
}

