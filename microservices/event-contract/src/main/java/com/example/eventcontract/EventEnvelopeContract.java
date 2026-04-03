package com.example.eventcontract;

/**
 * Contract constants shared by all Kafka event publishers/consumers.
 */
public final class EventEnvelopeContract {
    private EventEnvelopeContract() {
    }

    public static final String SCHEMA_VERSION_V1 = "1";

    // Backward-compat header key used in the current codebase.
    public static final String EVENT_TYPE_HEADER = "event-type";

    public static final String CORRELATION_ID_HEADER = "correlation-id";

    public static final String RESERVATION_EVENTS_TOPIC = "reservation-events";
    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    public static final String EVENT_RESERVATION_CREATED = "RESERVATION_CREATED";
    public static final String EVENT_RESERVATION_CONFIRMED = "RESERVATION_CONFIRMED";
    public static final String EVENT_RESERVATION_CANCELLED = "RESERVATION_CANCELLED";

    public static final String EVENT_PAYMENT_INITIATED = "PAYMENT_INITIATED";
    public static final String EVENT_PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String EVENT_PAYMENT_FAILED = "PAYMENT_FAILED";
}

