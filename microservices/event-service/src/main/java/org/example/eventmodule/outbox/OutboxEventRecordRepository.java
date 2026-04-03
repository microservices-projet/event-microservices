package org.example.eventmodule.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRecordRepository extends JpaRepository<OutboxEventRecord, Long> {
    List<OutboxEventRecord> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}

