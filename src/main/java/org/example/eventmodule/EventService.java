package org.example.eventmodule;

import org.example.eventmodule.Event;
import org.example.eventmodule.EventStatus;
import org.example.eventmodule.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event create(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @Transactional
    public Event update(Event event) {
        return eventRepository.save(event);
    }

    @Transactional
    public void archive(Long id) {
        Event event = getById(id);
        event.setArchived(true);
        eventRepository.save(event);
    }

    @Transactional
    public Event likeEvent(Long id) {
        Event event = getById(id);
        event.setNbLikes(event.getNbLikes() + 1);
        return eventRepository.save(event);
    }

    public List<Event> getByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    public List<Event> search(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }
}