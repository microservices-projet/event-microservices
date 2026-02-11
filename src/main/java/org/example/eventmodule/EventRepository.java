package org.example.eventmodule;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository  extends CrudRepository<Event, Long> {

    List<Event> findByStatus(String status);

    List<Event> findByArchivedFalse();

}
