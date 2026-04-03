package com.example.analyticsstreams.schedule;

import com.example.analyticsstreams.mongo.AnalyticsSnapshot;
import com.example.analyticsstreams.mongo.AnalyticsSnapshotRepository;
import com.example.analyticsstreams.service.AnalyticsDashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsSnapshotScheduler {

    private final AnalyticsDashboardService dashboardService;
    private final AnalyticsSnapshotRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 60_000)
    public void persistSnapshot() {
        try {
            String json = objectMapper.writeValueAsString(dashboardService.buildDashboard());
            snapshotRepository.save(AnalyticsSnapshot.builder()
                    .id(UUID.randomUUID().toString())
                    .capturedAt(Instant.now())
                    .payloadJson(json)
                    .build());
        } catch (Exception e) {
            log.warn("Analytics snapshot skipped: {}", e.getMessage());
        }
    }
}
