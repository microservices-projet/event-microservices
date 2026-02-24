package org.example.eventmodule;

import org.example.eventmodule.Event;
import org.example.eventmodule.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByTitleContainingIgnoreCase(String keyword);
    List<Event> findByStatus(EventStatus status);
}