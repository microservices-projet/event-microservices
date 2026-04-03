package org.example.eventmodule.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "domain_event_records")
@Getter
@Setter
public class DomainEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false, length = 128)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(length = 128)
    private String correlationId;

    @Column(length = 128)
    private String causationId;

    @Column(length = 128)
    private String actor;

    // Domain payload JSON (entity JSON). Kept as immutable string.
    @Lob
    @Column(nullable = false)
    private String payloadJson;
}

