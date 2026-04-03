package com.saladin.admin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_kafka_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedKafkaEvent {

    @Id
    private String eventId;

    private String topic;

    private LocalDateTime processedAt;
}

