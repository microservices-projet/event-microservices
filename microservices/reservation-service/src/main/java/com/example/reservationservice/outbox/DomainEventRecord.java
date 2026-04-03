package com.example.reservationservice.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "domain_event_records")
@Getter
@Setter
public class DomainEventRecord {

    @Id
    private String id;

    @Indexed(unique = true)
    private String eventId;

    private String aggregateType;

    private String aggregateId;

    private String eventType;

    private Instant occurredAt;

    private String correlationId;

    private String causationId;

    private String actor;

    // Domain payload JSON (entity JSON). Kept as immutable string.
    private String payloadJson;
}

