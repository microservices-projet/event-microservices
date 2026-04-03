package com.example.eventcontract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventEnvelopeContractTest {

    @Test
    void v1Envelope_hasRequiredFieldsAndPayload() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        Map<String, Object> payload = Map.of(
                "id", 1,
                "title", "Test Event"
        );

        EventEnvelope envelope = EventEnvelope.v1(
                "CREATED",
                "Event",
                "1",
                payload,
                null,
                null,
                null
        );

        String json = mapper.writeValueAsString(envelope);
        JsonNode root = mapper.readTree(json);

        assertEquals(EventEnvelopeContract.SCHEMA_VERSION_V1, root.get("schemaVersion").asText());
        assertEquals("CREATED", root.get("eventType").asText());
        assertNotNull(root.get("eventId"));
        assertNotNull(root.get("occurredAt"));
        assertNotNull(root.get("payload"));

        JsonNode payloadNode = root.get("payload");
        assertEquals(1, payloadNode.get("id").asInt());
        assertEquals("Test Event", payloadNode.get("title").asText());
    }
}

