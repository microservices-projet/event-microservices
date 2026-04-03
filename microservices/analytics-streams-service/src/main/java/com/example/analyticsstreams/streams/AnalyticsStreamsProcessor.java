package com.example.analyticsstreams.streams;

import com.example.analyticsstreams.metrics.AnalyticsSideEffects;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
@Slf4j
public class AnalyticsStreamsProcessor implements InitializingBean, DisposableBean {

    public static final String STORE_5M = "win-res-5m";
    public static final String STORE_1H = "win-res-1h";

    private final ObjectMapper objectMapper;
    private final AnalyticsSideEffects sideEffects;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${analytics.kafka-streams.enabled:true}")
    private boolean kafkaStreamsEnabled;

    @Value("${analytics.kafka-streams.state-dir}")
    private String streamsStateDir;

    @Getter
    private volatile KafkaStreams streams;

    public AnalyticsStreamsProcessor(ObjectMapper objectMapper, AnalyticsSideEffects sideEffects) {
        this.objectMapper = objectMapper;
        this.sideEffects = sideEffects;
    }

    @Override
    public void afterPropertiesSet() {
        if (!kafkaStreamsEnabled) {
            log.warn("Kafka Streams disabled (analytics.kafka-streams.enabled=false); windowed aggregates will be empty");
            return;
        }
        try {
            StreamsBuilder builder = new StreamsBuilder();

            KStream<String, String> reservationEvents = builder.stream(
                    "reservation-events",
                    Consumed.with(Serdes.String(), Serdes.String())
            );
            reservationEvents.peek((k, v) -> sideEffects.onReservationEnvelope(v));
            KStream<String, Long> resByEvent = reservationEvents.flatMap((k, v) -> confirmedReservationKeys(v));
            Materialized<String, Long, WindowStore<Bytes, byte[]>> mat5m =
                    Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(STORE_5M)
                            .withKeySerde(Serdes.String())
                            .withValueSerde(Serdes.Long());
            Materialized<String, Long, WindowStore<Bytes, byte[]>> mat1h =
                    Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(STORE_1H)
                            .withKeySerde(Serdes.String())
                            .withValueSerde(Serdes.Long());
            // Repartition topics must use Long values (counts), not DEFAULT_VALUE_SERDE (String).
            resByEvent.groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                    .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                    .count(mat5m);
            resByEvent.groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                    .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1)))
                    .count(mat1h);

            KStream<String, String> eventEvents = builder.stream(
                    "event-events",
                    Consumed.with(Serdes.String(), Serdes.String())
            );
            eventEvents.peek((k, v) -> sideEffects.onEventEnvelope(v));

            KStream<String, String> ticketEvents = builder.stream(
                    "ticket-events",
                    Consumed.with(Serdes.String(), Serdes.String())
            );
            ticketEvents.peek((k, v) -> sideEffects.onTicketEnvelope(v));

            Topology topology = builder.build();

            Properties config = new Properties();
            // Bump id when topology/serde fixes require clean internal topics (dev-friendly).
            config.put(StreamsConfig.APPLICATION_ID_CONFIG, "analytics-streams-v2");
            config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            config.put(StreamsConfig.STATE_DIR_CONFIG, streamsStateDir);

            streams = new KafkaStreams(topology, config);
            streams.start();
            log.info("Kafka Streams started (applicationId=analytics-streams-v2, bootstrap={})", bootstrapServers);
        } catch (Exception e) {
            streams = null;
            log.error(
                    "Kafka Streams not started — API / dashboard still up (in-memory + Redis tallies). "
                            + "Start Kafka (e.g. docker compose) and restart. Cause: {}",
                    e.getMessage());
            log.debug("Kafka Streams startup detail", e);
        }
    }

    @Override
    public void destroy() {
        if (streams != null) {
            streams.close();
        }
    }

    private List<KeyValue<String, Long>> confirmedReservationKeys(String envelopeJson) {
        try {
            JsonNode root = objectMapper.readTree(envelopeJson);
            String eventType = root.hasNonNull("eventType") ? root.get("eventType").asText() : null;
            if (!"CONFIRMED".equals(eventType)) {
                return List.of();
            }
            JsonNode payload = root.get("payload");
            if (payload == null || !payload.hasNonNull("eventId")) {
                return List.of();
            }
            String eventId = payload.get("eventId").asText();
            return List.of(KeyValue.pair(eventId, 1L));
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Sums windowed counts per eventId for store over the last {@code lookback}.
     */
    public Map<String, Long> sumWindowedCounts(String storeName, Duration lookback) {
        KafkaStreams ks = streams;
        if (ks == null || ks.state() != KafkaStreams.State.RUNNING) {
            return Map.of();
        }
        try {
            ReadOnlyWindowStore<String, Long> store = ks.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.windowStore())
            );
            long nowMs = System.currentTimeMillis();
            long fromMs = nowMs - lookback.toMillis();
            java.time.Instant from = java.time.Instant.ofEpochMilli(fromMs);
            java.time.Instant to = java.time.Instant.ofEpochMilli(nowMs);
            Map<String, Long> merged = new HashMap<>();
            try (KeyValueIterator<Windowed<String>, Long> it = store.fetchAll(from, to)) {
                while (it.hasNext()) {
                    var next = it.next();
                    if (next.value != null && next.value > 0) {
                        String key = next.key.key();
                        merged.merge(key, next.value, Long::sum);
                    }
                }
            }
            return merged;
        } catch (Exception e) {
            log.debug("Window store {} not queryable yet: {}", storeName, e.getMessage());
            return Map.of();
        }
    }

    public Map<Long, Long> confirmedReservationsLastMinutes(int minutes) {
        return sideEffects.confirmedReservationsLastMinutes(minutes);
    }

    public List<AnalyticsSideEffects.TopTitleCount> topCreatedEventTitles(int limit) {
        return sideEffects.topCreatedEventTitles(limit);
    }

    public Long redisTicketsSold() {
        return sideEffects.redisTicketSoldTotal();
    }
}
