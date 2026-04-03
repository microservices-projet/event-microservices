package com.example.reservationservice.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "outbox_event_records")
@Getter
@Setter
public class OutboxEventRecord {

    @Id
    private String id;

    private String domainEventId;

    private String kafkaTopic;

    private OutboxStatus status = OutboxStatus.PENDING;

    private int attempts = 0;

    private String lastError;

    private Instant createdAt = Instant.now();

    private Instant sentAt;
}

