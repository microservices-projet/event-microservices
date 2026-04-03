package com.example.analyticsstreams.service;

import com.example.analyticsstreams.streams.AnalyticsStreamsProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsDashboardService {

    private final AnalyticsStreamsProcessor streamsProcessor;

    public Map<String, Object> buildDashboard() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("reservationsByEventIdAggregated5mWindows", streamsProcessor.sumWindowedCounts(
                AnalyticsStreamsProcessor.STORE_5M, Duration.ofHours(1)));
        m.put("reservationsByEventIdAggregated1hWindows", streamsProcessor.sumWindowedCounts(
                AnalyticsStreamsProcessor.STORE_1H, Duration.ofHours(24)));
        m.put("reservationsPerMinuteSeriesLast60", streamsProcessor.confirmedReservationsLastMinutes(60));
        m.put("topCreatedEventTitles", streamsProcessor.topCreatedEventTitles(10));
        m.put("ticketsSoldCounterRedis", streamsProcessor.redisTicketsSold());
        m.put("generatedAt", Instant.now().toString());
        return m;
    }
}
