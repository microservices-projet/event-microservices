package org.example.eventmodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.eventmodule.Event;
import org.example.eventmodule.EventRepository;
import org.example.eventmodule.EventStatus;
import org.example.eventmodule.kafka.OptimizedEventKafkaConsumer;
import org.example.eventmodule.client.UserServiceClient;
import org.example.eventmodule.client.UserServiceClientConfig.*;
import org.example.eventmodule.request.EventRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Optimized Event Service with CRUD + Caching + Kafka Integration
 * Implements best practices for microservices communication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OptimizedEventService {

    private final EventRepository eventRepository;
    private final OptimizedEventKafkaConsumer kafkaConsumer;
    private final UserServiceClient userServiceClient;

    // ==================== CREATE ====================

    /**
     * Create new event with validation and Kafka notification
     */
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public Event createEvent(EventRequest request) {
        log.info("→ Creating event: {}", request.getTitle());

        try {
            // Validate organizer exists in User Service
            validateOrganizer(request.getOrganizerId());

            // Create event
            Event event = Event.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .date(request.getDate())
                    .place(request.getPlace())
                    .price(request.getPrice())
                    .organizerId(request.getOrganizerId())
                    .imageUrl(request.getImageUrl())
                    .nbPlaces(request.getNbPlaces())
                    .nbLikes(0)
                    .domaines(request.getDomaines())
                    .status(EventStatus.DRAFT)
                    .archived(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            Event savedEvent = eventRepository.save(event);
            log.info("✓ Event created successfully: id={}, title={}", savedEvent.getId(), savedEvent.getTitle());

            // Publish CREATED event to Kafka
            kafkaConsumer.sendEventCreated(savedEvent);

            return savedEvent;
        } catch (UserNotFoundException e) {
            log.error("✗ Failed to create event: Organizer not found - {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("✗ Failed to create event: {}", e.getMessage());
            throw new RuntimeException("Error creating event: " + e.getMessage());
        }
    }

    // ==================== READ ====================

    /**
     * Get all events (cached)
     */
    @Cacheable(value = "events", key = "'all'")
    public List<Event> getAllEvents() {
        log.info("→ Fetching all events");
        List<Event> events = eventRepository.findAll();
        log.info("✓ Retrieved {} events", events.size());
        return events;
    }

    /**
     * Get event by ID (cached)
     */
    @Cacheable(value = "events", key = "#id")
    public Event getEventById(Long id) {
        log.info("→ Fetching event: id={}", id);
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("✗ Event not found: id={}", id);
                    return new RuntimeException("Event not found: " + id);
                });

        // Publish FETCHED event to Kafka for analytics
        kafkaConsumer.sendEventFetched(event);
        return event;
    }

    /**
     * Get events by organizer
     */
    public List<Event> getEventsByOrganizer(Long organizerId) {
        log.info("→ Fetching events for organizer: id={}", organizerId);
        try {
            validateOrganizer(organizerId);
            List<Event> events = eventRepository.findByOrganizerId(organizerId);
            log.info("✓ Retrieved {} events for organizer {}", events.size(), organizerId);
            return events;
        } catch (UserNotFoundException e) {
            log.error("✗ Organizer not found: id={}", organizerId);
            throw e;
        }
    }

    /**
     * Search events by keyword
     */
    public List<Event> searchEvents(String keyword) {
        log.info("→ Searching events: keyword={}", keyword);
        List<Event> results = eventRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        log.info("✓ Found {} events matching keyword: {}", results.size(), keyword);

        // Publish SEARCHED event to Kafka for analytics
        kafkaConsumer.sendEventSearched(keyword, results.size());
        return results;
    }

    // ==================== UPDATE ====================

    /**
     * Update event with validation and Kafka notification
     */
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public Event updateEvent(Long id, EventRequest request) {
        log.info("→ Updating event: id={}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("✗ Event not found for update: id={}", id);
                    return new RuntimeException("Event not found: " + id);
                });

        try {
            // Update fields
            event.setTitle(request.getTitle());
            event.setDescription(request.getDescription());
            event.setDate(request.getDate());
            event.setPlace(request.getPlace());
            event.setPrice(request.getPrice());
            event.setImageUrl(request.getImageUrl());
            event.setNbPlaces(request.getNbPlaces());
            event.setDomaines(request.getDomaines());
            event.setUpdatedAt(LocalDateTime.now());

            Event updatedEvent = eventRepository.save(event);
            log.info("✓ Event updated successfully: id={}", updatedEvent.getId());

            // Publish UPDATED event to Kafka
            kafkaConsumer.sendEventUpdated(updatedEvent);

            return updatedEvent;
        } catch (Exception e) {
            log.error("✗ Failed to update event: {}", e.getMessage());
            throw new RuntimeException("Error updating event: " + e.getMessage());
        }
    }

    /**
     * Update event status
     */
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public Event updateEventStatus(Long id, EventStatus status) {
        log.info("→ Updating event status: id={}, status={}", id, status);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        event.setStatus(status);
        event.setUpdatedAt(LocalDateTime.now());
        Event updated = eventRepository.save(event);

        log.info("✓ Event status updated: id={}, status={}", id, status);
        kafkaConsumer.sendEventUpdated(updated);

        return updated;
    }

    /**
     * Publish event
     */
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public Event publishEvent(Long id) {
        return updateEventStatus(id, EventStatus.PUBLISHED);
    }

    // ==================== DELETE ====================

    /**
     * Archive/Delete event with Kafka notification
     */
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public void deleteEvent(Long id) {
        log.info("→ Deleting event: id={}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("✗ Event not found for deletion: id={}", id);
                    return new RuntimeException("Event not found: " + id);
                });

        try {
            event.setArchived(true);
            event.setUpdatedAt(LocalDateTime.now());
            eventRepository.save(event);
            log.info("✓ Event archived successfully: id={}", id);

            // Publish DELETED event to Kafka
            kafkaConsumer.sendEventDeleted(id, event.getTitle());
        } catch (Exception e) {
            log.error("✗ Failed to delete event: {}", e.getMessage());
            throw new RuntimeException("Error deleting event: " + e.getMessage());
        }
    }

    /**
     * Like event
     */
    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public Event likeEvent(Long id, Long userId) {
        log.info("→ Liking event: id={}, userId={}", id, userId);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found: " + id));

        event.setNbLikes(event.getNbLikes() + 1);
        event.setUpdatedAt(LocalDateTime.now());
        Event updated = eventRepository.save(event);

        log.info("✓ Event liked: id={}, totalLikes={}", id, updated.getNbLikes());
        kafkaConsumer.sendEventLiked(id, userId, updated.getNbLikes());

        return updated;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Validate organizer exists via Feign client
     */
    private void validateOrganizer(Long organizerId) {
        try {
            Boolean exists = userServiceClient.userExists(organizerId);
            if (!exists) {
                throw new UserNotFoundException("Organizer not found: " + organizerId);
            }
            log.debug("✓ Organizer validated: id={}", organizerId);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (UserServiceUnavailableException e) {
            log.warn("⚠ User service unavailable, proceeding with event creation");
        } catch (Exception e) {
            log.warn("⚠ Failed to validate organizer: {}", e.getMessage());
        }
    }
}

