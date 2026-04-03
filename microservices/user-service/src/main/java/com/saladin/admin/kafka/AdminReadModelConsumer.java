package com.saladin.admin.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saladin.admin.entity.AdminEventView;
import com.saladin.admin.entity.AdminFeedbackView;
import com.saladin.admin.entity.AdminReclamationView;
import com.saladin.admin.entity.AdminReservationView;
import com.saladin.admin.entity.AdminTicketView;
import com.saladin.admin.entity.AdminUserView;
import com.saladin.admin.repository.AdminEventViewRepository;
import com.saladin.admin.repository.AdminFeedbackViewRepository;
import com.saladin.admin.repository.AdminReclamationViewRepository;
import com.saladin.admin.repository.AdminReservationViewRepository;
import com.saladin.admin.repository.AdminTicketViewRepository;
import com.saladin.admin.repository.AdminUserViewRepository;
import com.saladin.admin.entity.ProcessedKafkaEvent;
import com.saladin.admin.repository.ProcessedKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminReadModelConsumer {
    private final ObjectMapper objectMapper;
    private final AdminUserViewRepository userRepo;
    private final AdminEventViewRepository eventRepo;
    private final AdminReservationViewRepository reservationRepo;
    private final AdminTicketViewRepository ticketRepo;
    private final AdminFeedbackViewRepository feedbackRepo;
    private final AdminReclamationViewRepository reclamationRepo;
    private final ProcessedKafkaEventRepository processedEventRepository;

    /**
     * Single consumer group member subscribing to all admin projection topics.
     * Do not use multiple {@code @KafkaListener} methods with the same {@code groupId} and different
     * topics — that violates Kafka's consumer group contract and causes endless rebalances.
     */
    @KafkaListener(
            topics = {
                    "user-events",
                    "event-events",
                    "reservation-events",
                    "ticket-events",
                    "feedback-events",
                    "reclamation-events"
            },
            groupId = "user-service-admin-read-model"
    )
    public void consumeAdminProjection(
            String payload,
            @Header(value = "event-type", required = false) String eventType,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        switch (topic) {
            case "user-events" -> consumeUserEvent(payload, eventType);
            case "event-events" -> consumeEventEvent(payload, eventType);
            case "reservation-events" -> consumeReservationEvent(payload, eventType);
            case "ticket-events" -> consumeTicketEvent(payload, eventType);
            case "feedback-events" -> consumeFeedbackEvent(payload, eventType);
            case "reclamation-events" -> consumeReclamationEvent(payload, eventType);
            default -> log.warn("Ignoring unknown topic for admin read model: {}", topic);
        }
    }

    private void consumeUserEvent(String payload, String eventType) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode n = root.has("payload") ? root.get("payload") : root;
            String effectiveEventType = root.has("eventType") ? text(root, "eventType") : eventType;
            String kafkaEventId = text(root, "eventId");

            if ("DELETED".equals(effectiveEventType)) {
                Long id = idAsLong(n);
                String dedupe = dedupeKey(kafkaEventId, "user-events", effectiveEventType, id == null ? null : "id:" + id);
                if (dedupe != null && processedEventRepository.existsById(dedupe)) {
                    return;
                }
                if (id != null) userRepo.deleteById(id);
                markProcessed(dedupe, "user-events");
                return;
            }

            Long id = longVal(n, "id");
            String dedupe = dedupeKey(kafkaEventId, "user-events", effectiveEventType, id == null ? null : "id:" + id);
            if (dedupe != null && processedEventRepository.existsById(dedupe)) {
                return;
            }

            if (id == null) return;
            userRepo.save(AdminUserView.builder()
                    .id(id)
                    .username(text(n, "username"))
                    .email(text(n, "email"))
                    .role(text(n, "role"))
                    .status(text(n, "status"))
                    .createdAt(date(n, "createdAt"))
                    .build());
            markProcessed(dedupe, "user-events");
        } catch (Exception e) {
            log.warn("Failed user-events projection", e);
            throw new RuntimeException(e);
        }
    }

    private void consumeEventEvent(String payload, String eventType) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode n = root.has("payload") ? root.get("payload") : root;
            String effectiveEventType = root.has("eventType") ? text(root, "eventType") : eventType;
            String kafkaEventId = text(root, "eventId");

            Long id = longVal(n, "id");
            String dedupe = dedupeKey(kafkaEventId, "event-events", effectiveEventType, id == null ? null : "id:" + id);
            if (dedupe != null && processedEventRepository.existsById(dedupe)) {
                return;
            }
            if (id == null) return;
            if ("DELETED".equals(effectiveEventType)) {
                eventRepo.deleteById(id);
                markProcessed(dedupe, "event-events");
                return;
            }
            eventRepo.save(AdminEventView.builder()
                    .id(id)
                    .title(text(n, "title"))
                    .description(text(n, "description"))
                    .date(date(n, "date"))
                    .place(text(n, "place"))
                    .price(doubleVal(n, "price"))
                    .organizerId(longVal(n, "organizerId"))
                    .imageUrl(text(n, "imageUrl"))
                    .nbPlaces(intVal(n, "nbPlaces"))
                    .nbLikes(intVal(n, "nbLikes"))
                    .domainesJson(json(n, "domaines"))
                    .status(text(n, "status"))
                    .archived(boolVal(n, "archived"))
                    .createdAt(date(n, "createdAt"))
                    .updatedAt(date(n, "updatedAt"))
                    .build());
            markProcessed(dedupe, "event-events");
        } catch (Exception e) {
            log.warn("Failed event-events projection", e);
            throw new RuntimeException(e);
        }
    }

    private void consumeReservationEvent(String payload, String eventType) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode n = root.has("payload") ? root.get("payload") : root;
            String effectiveEventType = root.has("eventType") ? text(root, "eventType") : eventType;
            String kafkaEventId = text(root, "eventId");

            String id = text(n, "id");
            String dedupe = dedupeKey(kafkaEventId, "reservation-events", effectiveEventType, id == null || id.isBlank() ? null : "id:" + id);
            if (dedupe != null && processedEventRepository.existsById(dedupe)) {
                return;
            }
            if (id == null || id.isBlank()) return;
            if ("DELETED".equals(effectiveEventType)) {
                reservationRepo.deleteById(id);
                markProcessed(dedupe, "reservation-events");
                return;
            }
            Long userId = longVal(n, "userId");
            Long eventId = longVal(n, "eventId");
            AdminReservationView view = AdminReservationView.builder()
                    .id(id)
                    .userId(userId)
                    .eventId(eventId)
                    .ticketId(longVal(n, "ticketId"))
                    .numberOfPlaces(intVal(n, "numberOfPlaces"))
                    .totalPrice(doubleVal(n, "totalPrice"))
                    .status(text(n, "status"))
                    .paymentStatus(text(n, "paymentStatus"))
                    .reservationDate(date(n, "reservationDate"))
                    .confirmationDate(date(n, "confirmationDate"))
                    .cancellationDate(date(n, "cancellationDate"))
                    .cancellationReason(text(n, "cancellationReason"))
                    .auditLogJson(json(n, "auditLog"))
                    .createdAt(date(n, "createdAt"))
                    .updatedAt(date(n, "updatedAt"))
                    .username(userId == null ? null : userRepo.findById(userId).map(AdminUserView::getUsername).orElse(null))
                    .eventTitle(eventId == null ? null : eventRepo.findById(eventId).map(AdminEventView::getTitle).orElse(null))
                    .eventDate(eventId == null ? null : eventRepo.findById(eventId).map(AdminEventView::getDate).orElse(null))
                    .build();
            reservationRepo.save(view);
            markProcessed(dedupe, "reservation-events");
        } catch (Exception e) {
            log.warn("Failed reservation-events projection", e);
            throw new RuntimeException(e);
        }
    }

    private void consumeTicketEvent(String payload, String eventType) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode n = root.has("payload") ? root.get("payload") : root;
            String effectiveEventType = root.has("eventType") ? text(root, "eventType") : eventType;
            String kafkaEventId = text(root, "eventId");

            Long id = longVal(n, "idTicket");
            String dedupe = dedupeKey(kafkaEventId, "ticket-events", effectiveEventType, id == null ? null : "idTicket:" + id);
            if (dedupe != null && processedEventRepository.existsById(dedupe)) {
                return;
            }

            if ("DELETED".equals(effectiveEventType)) {
                Long delId = id != null ? id : idAsLong(n);
                if (delId != null) ticketRepo.deleteById(delId);
                markProcessed(dedupe, "ticket-events");
                return;
            }

            if (id == null) return;
            ticketRepo.save(AdminTicketView.builder()
                    .idTicket(id)
                    .eventId(longVal(n, "eventId"))
                    .userId(longVal(n, "userId"))
                    .nomClient(text(n, "nomClient"))
                    .emailClient(text(n, "emailClient"))
                    .prix(doubleVal(n, "prix"))
                    .eventTitle(text(n, "eventTitle"))
                    .statut(text(n, "statut"))
                    .typeTicket(text(n, "typeTicket"))
                    .nombreMaxTickets(intVal(n, "nombreMaxTickets"))
                    .dateCreation(date(n, "dateCreation"))
                    .build());
            markProcessed(dedupe, "ticket-events");
        } catch (Exception e) {
            log.warn("Failed ticket-events projection", e);
            throw new RuntimeException(e);
        }
    }

    private void consumeFeedbackEvent(String payload, String eventType) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode n = root.has("payload") ? root.get("payload") : root;
            String effectiveEventType = root.has("eventType") ? text(root, "eventType") : eventType;
            String kafkaEventId = text(root, "eventId");

            Long id = longVal(n, "id");
            String dedupe = dedupeKey(kafkaEventId, "feedback-events", effectiveEventType, id == null ? null : "id:" + id);
            if (dedupe != null && processedEventRepository.existsById(dedupe)) {
                return;
            }

            if ("DELETED".equals(effectiveEventType)) {
                Long delId = id != null ? id : idAsLong(n);
                if (delId != null) feedbackRepo.deleteById(delId);
                markProcessed(dedupe, "feedback-events");
                return;
            }

            if (id == null) return;
            Long userId = longVal(n, "userId");
            Long eventId = longVal(n, "eventId");
            feedbackRepo.save(AdminFeedbackView.builder()
                    .id(id)
                    .eventId(eventId)
                    .userId(userId)
                    .rating(intVal(n, "rating"))
                    .comment(text(n, "comment"))
                    .status(text(n, "status"))
                    .moderatedBy(longVal(n, "moderatedBy"))
                    .moderationNote(text(n, "moderationNote"))
                    .flaggedReason(text(n, "flaggedReason"))
                    .createdAt(date(n, "createdAt"))
                    .updatedAt(date(n, "updatedAt"))
                    .username(userId == null ? null : userRepo.findById(userId).map(AdminUserView::getUsername).orElse(null))
                    .eventTitle(eventId == null ? null : eventRepo.findById(eventId).map(AdminEventView::getTitle).orElse(null))
                    .build());
            markProcessed(dedupe, "feedback-events");
        } catch (Exception e) {
            log.warn("Failed feedback-events projection", e);
            throw new RuntimeException(e);
        }
    }

    private void consumeReclamationEvent(String payload, String eventType) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode n = root.has("payload") ? root.get("payload") : root;
            String effectiveEventType = root.has("eventType") ? text(root, "eventType") : eventType;
            String kafkaEventId = text(root, "eventId");

            Long id = longVal(n, "id");
            String dedupe = dedupeKey(kafkaEventId, "reclamation-events", effectiveEventType, id == null ? null : "id:" + id);
            if (dedupe != null && processedEventRepository.existsById(dedupe)) {
                return;
            }

            if ("DELETED".equals(effectiveEventType)) {
                Long delId = id != null ? id : idAsLong(n);
                if (delId != null) reclamationRepo.deleteById(delId);
                markProcessed(dedupe, "reclamation-events");
                return;
            }

            if (id == null) return;
            Long userId = longVal(n, "userId");
            Long eventId = longVal(n, "eventId");
            reclamationRepo.save(AdminReclamationView.builder()
                    .id(id)
                    .userId(userId)
                    .eventId(eventId)
                    .reservationId(text(n, "reservationId"))
                    .ticketId(longVal(n, "ticketId"))
                    .subject(text(n, "subject"))
                    .description(text(n, "description"))
                    .type(text(n, "type"))
                    .status(text(n, "status"))
                    .priority(text(n, "priority"))
                    .assignedTo(longVal(n, "assignedTo"))
                    .response(text(n, "response"))
                    .resolvedAt(date(n, "resolvedAt"))
                    .createdAt(date(n, "createdAt"))
                    .updatedAt(date(n, "updatedAt"))
                    .username(userId == null ? null : userRepo.findById(userId).map(AdminUserView::getUsername).orElse(null))
                    .eventTitle(eventId == null ? null : eventRepo.findById(eventId).map(AdminEventView::getTitle).orElse(null))
                    .build());
            markProcessed(dedupe, "reclamation-events");
        } catch (Exception e) {
            log.warn("Failed reclamation-events projection", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * When Kafka envelope omits {@code eventId}, dedupe only on kafka id is impossible and the same
     * business message can be applied repeatedly (duplicate admin rows / inflated counts). Fall back
     * to a stable synthetic key per topic + event type + business id.
     */
    private String dedupeKey(String kafkaEventId, String topic, String effectiveEventType, String businessKey) {
        if (kafkaEventId != null && !kafkaEventId.isBlank()) {
            return kafkaEventId;
        }
        if (businessKey == null || businessKey.isBlank()) {
            return null;
        }
        String type = effectiveEventType != null && !effectiveEventType.isBlank() ? effectiveEventType : "_";
        return topic + "::" + type + "::" + businessKey;
    }

    private String text(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private String json(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.toString();
    }

    private Integer intVal(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asInt();
    }

    private Long longVal(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asLong();
    }

    private Double doubleVal(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return (v == null || v.isNull()) ? null : v.asDouble();
    }

    private boolean boolVal(JsonNode n, String key) {
        JsonNode v = n.get(key);
        return v != null && !v.isNull() && v.asBoolean();
    }

    private LocalDateTime date(JsonNode n, String key) {
        String raw = text(n, key);
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception ignore) {
            return null;
        }
    }

    private Long idAsLong(JsonNode node) {
        if (node == null || node.isNull()) return null;
        try {
            if (node.isNumber()) return node.asLong();
            if (node.isTextual()) return Long.parseLong(node.asText());
            return node.asLong();
        } catch (Exception e) {
            return null;
        }
    }

    private void markProcessed(String eventId, String topic) {
        if (eventId == null || eventId.isBlank()) return;
        processedEventRepository.save(
                ProcessedKafkaEvent.builder()
                        .eventId(eventId)
                        .topic(topic)
                        .processedAt(LocalDateTime.now())
                        .build()
        );
    }
}
