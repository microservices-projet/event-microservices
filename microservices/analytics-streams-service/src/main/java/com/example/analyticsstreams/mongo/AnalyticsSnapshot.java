package com.example.analyticsstreams.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "analytics_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSnapshot {

    @Id
    private String id;

    private Instant capturedAt;

    /** JSON string of dashboard payload */
    private String payloadJson;
}
