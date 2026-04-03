package org.example.eventmodule.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "outbox_event_records")
@Getter
@Setter
public class OutboxEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String domainEventId;

    @Column(nullable = false, length = 255)
    private String kafkaTopic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(length = 2000)
    private String lastError;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant sentAt;
}

