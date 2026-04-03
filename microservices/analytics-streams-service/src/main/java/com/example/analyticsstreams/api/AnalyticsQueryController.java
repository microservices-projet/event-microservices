package com.example.analyticsstreams.api;

import com.example.analyticsstreams.metrics.AnalyticsSideEffects;
import com.example.analyticsstreams.service.AnalyticsDashboardService;
import com.example.analyticsstreams.streams.AnalyticsStreamsProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AnalyticsQueryController {

    private final AnalyticsStreamsProcessor processor;
    private final AnalyticsDashboardService dashboardService;

    @GetMapping("/api/analytics/reservations-per-minute")
    public Map<Long, Long> reservationsPerMinute(@RequestParam(defaultValue = "10") int minutes) {
        return processor.confirmedReservationsLastMinutes(minutes);
    }

    @GetMapping("/api/analytics/top-event-titles")
    public List<AnalyticsSideEffects.TopTitleCount> topEventTitles(@RequestParam(defaultValue = "5") int limit) {
        return processor.topCreatedEventTitles(limit);
    }

    @GetMapping("/api/analytics/dashboard")
    public Map<String, Object> dashboard() {
        return dashboardService.buildDashboard();
    }
}
