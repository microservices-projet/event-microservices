package com.example.analyticsstreams.metrics;

import com.example.analyticsstreams.kafka.AnalyticsAlertPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * In-memory tallies, optional Redis counters, and booking-velocity alert (MVP).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsSideEffects {

    private static final int VELOCITY_THRESHOLD = 25;
    private static final String REDIS_RV_PREFIX = "analytics:rv:minute:";
    private static final String REDIS_TICKETS = "analytics:tickets:sold";

    private final ObjectMapper objectMapper;
    private final AnalyticsAlertPublisher alertPublisher;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private final ConcurrentHashMap<Long, LongAdder> confirmedReservationsPerMinute = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongAdder> createdEventTitleCounts = new ConcurrentHashMap<>();

    private final AtomicLong velocityMinuteBucket = new AtomicLong(-1);
    private final AtomicInteger velocityCount = new AtomicInteger(0);
    private final AtomicLong lastAlertMinute = new AtomicLong(-1);

    public void onReservationEnvelope(String envelopeJson) {
        try {
            JsonNode root = objectMapper.readTree(envelopeJson);
            String eventType = text(root, "eventType");
            if (!"CONFIRMED".equals(eventType)) {
                return;
            }
            long minuteKey = minuteKey(root);
            confirmedReservationsPerMinute
                    .computeIfAbsent(minuteKey, __ -> new LongAdder())
                    .increment();

            if (redisTemplate != null) {
                redisTemplate.opsForValue().increment(REDIS_RV_PREFIX + minuteKey);
            }

            trackVelocityAlert(root);
        } catch (Exception e) {
            log.debug("Reservation side-effect skip: {}", e.getMessage());
        }
    }

    public void onTicketEnvelope(String envelopeJson) {
        try {
            JsonNode root = objectMapper.readTree(envelopeJson);
            String eventType = text(root, "eventType");
            if (!"CREATED".equals(eventType)) {
                return;
            }
            if (redisTemplate != null) {
                redisTemplate.opsForValue().increment(REDIS_TICKETS);
            }
        } catch (Exception e) {
            log.debug("Ticket side-effect skip: {}", e.getMessage());
        }
    }

    public void onEventEnvelope(String envelopeJson) {
        try {
            JsonNode root = objectMapper.readTree(envelopeJson);
            String eventType = text(root, "eventType");
            if (!"CREATED".equals(eventType)) {
                return;
            }
            JsonNode payload = root.get("payload");
            String title = payload != null && payload.hasNonNull("title") ? payload.get("title").asText() : null;
            if (title == null || title.isBlank()) {
                return;
            }
            createdEventTitleCounts
                    .computeIfAbsent(title, __ -> new LongAdder())
                    .increment();
        } catch (Exception e) {
            log.debug("Event side-effect skip: {}", e.getMessage());
        }
    }

    private void trackVelocityAlert(JsonNode root) {
        long minute = Instant.now().getEpochSecond() / 60;
        long prev = velocityMinuteBucket.get();
        if (prev != minute) {
            velocityMinuteBucket.set(minute);
            velocityCount.set(0);
        }
        int c = velocityCount.incrementAndGet();
        if (c >= VELOCITY_THRESHOLD) {
            synchronized (this) {
                if (lastAlertMinute.get() != minute) {
                    lastAlertMinute.set(minute);
                    alertPublisher.publishVelocityAlert(minute, c, root);
                }
            }
        }
    }

    public Map<Long, Long> confirmedReservationsLastMinutes(int minutes) {
        long nowMinute = Instant.now().getEpochSecond() / 60;
        long fromMinute = nowMinute - minutes;
        return confirmedReservationsPerMinute.entrySet().stream()
                .filter(e -> e.getKey() >= fromMinute && e.getKey() <= nowMinute)
                .sorted(Comparator.comparingLong(Map.Entry::getKey))
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().longValue(),
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ));
    }

    public java.util.List<TopTitleCount> topCreatedEventTitles(int limit) {
        return createdEventTitleCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().longValue(), a.getValue().longValue()))
                .limit(limit)
                .map(e -> new TopTitleCount(e.getKey(), e.getValue().longValue()))
                .toList();
    }

    public Long redisTicketSoldTotal() {
        if (redisTemplate == null) {
            return null;
        }
        String v = redisTemplate.opsForValue().get(REDIS_TICKETS);
        return v == null ? 0L : Long.parseLong(v);
    }

    private static long minuteKey(JsonNode root) {
        try {
            String occurredAt = root.hasNonNull("occurredAt") ? root.get("occurredAt").asText() : null;
            if (occurredAt == null) {
                return Instant.now().getEpochSecond() / 60;
            }
            return Instant.parse(occurredAt).getEpochSecond() / 60;
        } catch (Exception ignored) {
            return Instant.now().getEpochSecond() / 60;
        }
    }

    private static String text(JsonNode n, String field) {
        return n != null && n.hasNonNull(field) ? n.get(field).asText() : null;
    }

    public record TopTitleCount(String title, long count) {
    }
}
