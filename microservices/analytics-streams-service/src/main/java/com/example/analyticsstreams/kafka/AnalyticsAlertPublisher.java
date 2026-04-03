package com.example.analyticsstreams.kafka;

import com.example.eventcontract.EventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsAlertPublisher {

    public static final String TOPIC = "analytics-alerts";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:analytics-streams-service}")
    private String applicationName;

    public void publishVelocityAlert(long minuteBucket, int count, JsonNode sourceEnvelope) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("alertType", "BOOKING_VELOCITY");
            payload.put("minuteBucket", minuteBucket);
            payload.put("confirmedCount", count);
            payload.put("threshold", 25);
            if (sourceEnvelope != null) {
                payload.set("sampleEnvelope", sourceEnvelope);
            }
            EventEnvelope envelope = EventEnvelope.v1(
                    "ANALYTICS_ALERT",
                    "Analytics",
                    String.valueOf(minuteBucket),
                    payload,
                    null,
                    null,
                    applicationName
            );
            kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(envelope));
            log.warn("Published booking velocity alert minute={} count={}", minuteBucket, count);
        } catch (Exception e) {
            log.error("Failed to publish analytics alert: {}", e.getMessage());
        }
    }
}
