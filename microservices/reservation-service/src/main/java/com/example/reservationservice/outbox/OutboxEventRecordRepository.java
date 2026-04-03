package com.example.reservationservice.outbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OutboxEventRecordRepository extends MongoRepository<OutboxEventRecord, String> {
    List<OutboxEventRecord> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}

