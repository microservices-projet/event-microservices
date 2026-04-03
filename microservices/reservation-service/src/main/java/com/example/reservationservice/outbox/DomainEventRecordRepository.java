package com.example.reservationservice.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DomainEventRecordRepository extends MongoRepository<DomainEventRecord, String> {
    Optional<DomainEventRecord> findByEventId(String eventId);
}

