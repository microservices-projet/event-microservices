package com.example.eventcontract;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Versioned Kafka event envelope.
 *
 * Design goal: keep your current payload structure (entity JSON) inside {@code payload}
 * so existing projections can be migrated incrementally.
 */
public class EventEnvelope {
    // Keep as String for stable contract even if numeric comparisons change later.
    private String schemaVersion = EventEnvelopeContract.SCHEMA_VERSION_V1;
    private String eventId;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private Instant occurredAt;
    private String correlationId;
    private String causationId;
    private String actor;
    private Object payload;

    public EventEnvelope() {
        // For Jackson
    }

    public EventEnvelope(
            String eventType,
            String aggregateType,
            String aggregateId,
            Object payload,
            String correlationId,
            String causationId,
            String actor
    ) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.occurredAt = Instant.now();
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.actor = actor;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getEventId() {
        return eventId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getCausationId() {
        return causationId;
    }

    public String getActor() {
        return actor;
    }

    public Object getPayload() {
        return payload;
    }

    // Setters (used when we parse envelopes in consumers)
    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setCausationId(String causationId) {
        this.causationId = causationId;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public static EventEnvelope v1(
            String eventType,
            String aggregateType,
            String aggregateId,
            Object payload,
            String correlationId,
            String causationId,
            String actor
    ) {
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(aggregateType, "aggregateType");
        Objects.requireNonNull(aggregateId, "aggregateId");
        return new EventEnvelope(eventType, aggregateType, aggregateId, payload, correlationId, causationId, actor);
    }

    /**
     * Optional helper: treat {@code payload} as a JsonNode when consumer logic wants tree operations.
     * If the underlying payload is not a JsonNode, consumers should parse the envelope JSON directly.
     */
    public JsonNode payloadAsNode(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        if (payload == null) return null;
        return objectMapper.valueToTree(payload);
    }
}

