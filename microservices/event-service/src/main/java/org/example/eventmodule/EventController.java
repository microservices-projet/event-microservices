package org.example.eventmodule;

import org.example.eventmodule.request.EventRequest;
import org.example.eventmodule.request.PlacesChangeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @PostMapping
    public ResponseEntity<Event> create(@RequestBody EventRequest request) {
        log.info("Creating event: {}", request.getTitle());

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .place(request.getPlace())
                .price(request.getPrice())
                .organizerId(request.getOrganizerId())
                .imageUrl(request.getImageUrl())
                .nbPlaces(request.getNbPlaces())
                .nbLikes(request.getNbLikes() != null ? request.getNbLikes() : 0)
                .domaines(request.getDomaines() != null ? request.getDomaines() : new ArrayList<>())
                .status(request.getStatus() != null ? request.getStatus() : EventStatus.PUBLISHED)
                .build();

        Event savedEvent = service.create(event);
        return ResponseEntity.ok(savedEvent);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable Long id, @RequestBody EventRequest request) {
        log.info("Updating event ID: {}", id);

        Event updatedEvent = service.updateFromRequest(id, request);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable Long id) {
        log.info("Archiving event ID: {}", id);
        service.archive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Event> like(@PathVariable Long id) {
        return ResponseEntity.ok(service.likeEvent(id));
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<Event>> getByOrganizer(@PathVariable Long organizerId) {
        return ResponseEntity.ok(service.getByOrganizer(organizerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(service.search(keyword));
    }

    @PostMapping("/{id}/reserve-places")
    public ResponseEntity<Event> reservePlaces(@PathVariable Long id, @Valid @RequestBody PlacesChangeRequest body) {
        try {
            return ResponseEntity.ok(service.reservePlaces(id, body.getPlaces()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/release-places")
    public ResponseEntity<Event> releasePlaces(@PathVariable Long id, @Valid @RequestBody PlacesChangeRequest body) {
        return ResponseEntity.ok(service.releasePlaces(id, body.getPlaces()));
    }
}
