package org.example.eventmodule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface EventRepository  extends JpaRepository<Event, Long> {

    List<Event> findByStatus(String status);

    List<Event> findByArchivedFalse();

}
