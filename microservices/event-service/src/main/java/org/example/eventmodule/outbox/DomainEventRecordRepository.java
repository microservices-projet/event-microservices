package org.example.eventmodule.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DomainEventRecordRepository extends JpaRepository<DomainEventRecord, Long> {
    Optional<DomainEventRecord> findByEventId(String eventId);

    List<DomainEventRecord> findByAggregateIdOrderByOccurredAtAsc(String aggregateId);
}

