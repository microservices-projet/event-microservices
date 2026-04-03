package org.example.eventmodule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.eventmodule.exception.EventNotFoundException;
import org.example.eventmodule.outbox.DomainEventRecord;
import org.example.eventmodule.outbox.DomainEventRecordRepository;
import org.example.eventmodule.outbox.OutboxEventRecord;
import org.example.eventmodule.outbox.OutboxEventRecordRepository;
import org.example.eventmodule.outbox.OutboxStatus;
import org.example.eventmodule.request.EventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

    private static final String AGGREGATE_TYPE = "Event";
    private static final String TOPIC = "event-events";

    private final EventRepository eventRepository;
    private final DomainEventRecordRepository domainEventRecordRepository;
    private final OutboxEventRecordRepository outboxEventRecordRepository;
    private final ObjectMapper objectMapper;
    private final EventRehydrator eventRehydrator;

    @Transactional
    public Event create(Event event) {
        Event saved = eventRepository.save(event);
        appendKafkaEvent(saved, "CREATED");
        return saved;
    }

    public List<Event> getAll() {
        return eventRepository.findByArchivedFalse();
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
    }

    @Transactional
    public Event update(Event event) {
        Event saved = eventRepository.save(event);
        appendKafkaEvent(saved, "UPDATED");
        return saved;
    }

    @Transactional
    public void archive(Long id) {
        Event event = eventRehydrator.rehydrate(id);
        event.setArchived(true);
        Event saved = eventRepository.save(event);
        appendKafkaEvent(saved, "DELETED");
    }

    @Transactional
    public Event likeEvent(Long id) {
        Event event = eventRehydrator.rehydrate(id);
        event.setNbLikes(event.getNbLikes() + 1);
        Event saved = eventRepository.save(event);
        appendKafkaEvent(saved, "UPDATED");
        return saved;
    }

    @Transactional
    public Event updateFromRequest(Long id, EventRequest request) {
        Event current = eventRehydrator.rehydrate(id);
        if (current.isArchived()) {
            throw new IllegalStateException("Cannot update an archived Event (id=" + id + ")");
        }

        // Apply command data onto the rehydrated aggregate snapshot.
        current.setTitle(request.getTitle());
        current.setDescription(request.getDescription());
        current.setDate(request.getDate());
        current.setPlace(request.getPlace());
        current.setPrice(request.getPrice());
        current.setOrganizerId(request.getOrganizerId());
        current.setImageUrl(request.getImageUrl());
        current.setNbPlaces(request.getNbPlaces());
        current.setDomaines(request.getDomaines() != null ? request.getDomaines() : List.of());
        current.setNbLikes(request.getNbLikes() != null ? request.getNbLikes() : current.getNbLikes());
        if (request.getStatus() != null) current.setStatus(request.getStatus());

        Event saved = eventRepository.save(current);
        appendKafkaEvent(saved, "UPDATED");
        return saved;
    }

    public List<Event> getByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    public List<Event> search(String keyword) {
        return eventRepository.findByTitleContainingIgnoreCase(keyword);
    }

    /**
     * Decrements available places for saga / booking flow. Skips enforcement when {@code nbPlaces} is null (unlimited).
     */
    @Transactional
    public Event reservePlaces(Long id, int places) {
        if (places <= 0) {
            throw new IllegalArgumentException("places must be positive");
        }
        Event event = eventRehydrator.rehydrate(id);
        if (event.isArchived()) {
            throw new IllegalStateException("Event archived id=" + id);
        }
        Integer nb = event.getNbPlaces();
        if (nb != null) {
            if (nb < places) {
                throw new IllegalStateException("Not enough places: requested=" + places + " available=" + nb);
            }
            event.setNbPlaces(nb - places);
        }
        Event saved = eventRepository.save(event);
        appendKafkaEvent(saved, "UPDATED");
        return saved;
    }

    @Transactional
    public Event releasePlaces(Long id, int places) {
        if (places <= 0) {
            throw new IllegalArgumentException("places must be positive");
        }
        Event event = eventRehydrator.rehydrate(id);
        if (event.isArchived()) {
            return event;
        }
        Integer nb = event.getNbPlaces();
        if (nb != null) {
            event.setNbPlaces(nb + places);
        }
        Event saved = eventRepository.save(event);
        appendKafkaEvent(saved, "UPDATED");
        return saved;
    }

    private void appendKafkaEvent(Event event, String eventType) {
        try {
            String eventId = UUID.randomUUID().toString();
            Instant occurredAt = Instant.now();
            String payloadJson = objectMapper.writeValueAsString(event);

            DomainEventRecord domain = new DomainEventRecord();
            domain.setEventId(eventId);
            domain.setAggregateType(AGGREGATE_TYPE);
            domain.setAggregateId(String.valueOf(event.getId()));
            domain.setEventType(eventType);
            domain.setOccurredAt(occurredAt);
            domain.setCorrelationId(null);
            domain.setCausationId(null);
            domain.setActor(null);
            domain.setPayloadJson(payloadJson);
            domainEventRecordRepository.save(domain);

            OutboxEventRecord outbox = new OutboxEventRecord();
            outbox.setDomainEventId(eventId);
            outbox.setKafkaTopic(TOPIC);
            outbox.setStatus(OutboxStatus.PENDING);
            outbox.setAttempts(0);
            outboxEventRecordRepository.save(outbox);
        } catch (Exception e) {
            // If we can't store the outbox, fail the command to avoid losing events.
            throw new IllegalStateException("Failed to append Kafka event to outbox for eventId=" + event.getId(), e);
        }
    }
}
