package org.example.eventmodule;

import org.example.eventmodule.Event;
import org.example.eventmodule.request.EventRequest;
import org.example.eventmodule.EventService;
import org.example.eventmodule.EventProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;
    private final EventProducerService eventProducer;

    @PostMapping
    public Event create(@RequestBody EventRequest request) {
        log.info("Création d'un nouvel événement: {}", request.getTitle());

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
        eventProducer.sendEvent(savedEvent, "CREATE");
        return savedEvent;
    }

    @GetMapping
    public List<Event> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Event getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable Long id, @RequestBody EventRequest request) {
        log.info("Mise à jour de l'événement ID: {}", id);
        Event event = service.getById(id);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setDate(request.getDate());
        event.setPlace(request.getPlace());
        event.setPrice(request.getPrice());
        event.setOrganizerId(request.getOrganizerId());
        event.setImageUrl(request.getImageUrl());
        event.setNbPlaces(request.getNbPlaces());
        event.setDomaines(request.getDomaines());
        event.setStatus(request.getStatus());

        Event updatedEvent = service.update(event);
        eventProducer.sendEvent(updatedEvent, "UPDATE");
        return updatedEvent;
    }

    @DeleteMapping("/{id}")
    public void archive(@PathVariable Long id) {
        log.info("Archivage de l'événement ID: {}", id);
        Event event = service.getById(id);
        service.archive(id);
        eventProducer.sendEvent(event, "DELETE");
    }

    @PostMapping("/{id}/like")
    public Event like(@PathVariable Long id) {
        return service.likeEvent(id);
    }

    @GetMapping("/organizer/{organizerId}")
    public List<Event> getByOrganizer(@PathVariable Long organizerId) {
        return service.getByOrganizer(organizerId);
    }

    @GetMapping("/search")
    public List<Event> search(@RequestParam String keyword) {
        return service.search(keyword);
    }
}