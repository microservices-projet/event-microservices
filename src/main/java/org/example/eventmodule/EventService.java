package org.example.eventmodule;

import org.example.eventmodule.Event;
import org.example.eventmodule.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository repository;

    // 🔹 Injection par constructeur
    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public Event create(Event event) {
        return repository.save(event);
    }

    public List<Event> getAll() {
        return repository.findByArchivedFalse();
    }

    public Event update(Long id, Event updatedEvent) {
        Event event = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setStartDate(updatedEvent.getStartDate());
        event.setEndDate(updatedEvent.getEndDate());
        event.setLocation(updatedEvent.getLocation());
        event.setStatus(updatedEvent.getStatus());

        return repository.save(event);
    }

    public void archive(Long id) {
        Event event = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setArchived(true);
        repository.save(event);
    }
}
