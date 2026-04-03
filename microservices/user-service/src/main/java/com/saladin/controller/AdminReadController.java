package com.saladin.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/admin")
@RequiredArgsConstructor
public class AdminReadController {
    private final AdminUserViewRepository userRepo;
    private final AdminEventViewRepository eventRepo;
    private final AdminReservationViewRepository reservationRepo;
    private final AdminTicketViewRepository ticketRepo;
    private final AdminFeedbackViewRepository feedbackRepo;
    private final AdminReclamationViewRepository reclamationRepo;
    private final ObjectMapper objectMapper;

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> dashboardSummary() {
        List<AdminReservationView> reservations = reservationRepo.findAll();
        long revenueCount = reservations.stream()
                .filter(r -> "PAID".equalsIgnoreCase(r.getPaymentStatus()))
                .count();
        double revenue = reservations.stream()
                .filter(r -> "PAID".equalsIgnoreCase(r.getPaymentStatus()))
                .map(AdminReservationView::getTotalPrice)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        Map<String, Long> reservationsByStatus = reservations.stream()
                .collect(Collectors.groupingBy(r -> safe(r.getStatus()), Collectors.counting()));
        Map<String, Long> ticketsByStatus = ticketRepo.findAll().stream()
                .collect(Collectors.groupingBy(t -> safe(t.getStatut()), Collectors.counting()));
        Map<String, Long> feedbacksByStatus = feedbackRepo.findAll().stream()
                .collect(Collectors.groupingBy(f -> safe(f.getStatus()), Collectors.counting()));
        Map<String, Long> reclamationsByStatus = reclamationRepo.findAll().stream()
                .collect(Collectors.groupingBy(r -> safe(r.getStatus()), Collectors.counting()));

        Map<String, Object> out = new HashMap<>();
        out.put("usersCount", userRepo.count());
        out.put("eventsCount", eventRepo.count());
        out.put("reservationsCount", reservationRepo.count());
        out.put("revenue", revenue);
        out.put("paidReservationsCount", revenueCount);
        out.put("reservationsByStatus", reservationsByStatus);
        out.put("ticketsByStatus", ticketsByStatus);
        out.put("feedbacksByStatus", feedbacksByStatus);
        out.put("reclamationsByStatus", reclamationsByStatus);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserView>> users(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        List<AdminUserView> out = userRepo.findAll().stream()
                .filter(u -> query == null || query.isBlank() ||
                        contains(u.getUsername(), query) || contains(u.getEmail(), query) || String.valueOf(u.getId()).contains(query))
                .filter(u -> role == null || role.isBlank() || role.equalsIgnoreCase(u.getRole()))
                .filter(u -> status == null || status.isBlank() || status.equalsIgnoreCase(u.getStatus()))
                .toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/events")
    public ResponseEntity<List<Map<String, Object>>> events(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Long organizerId) {
        List<Map<String, Object>> out = eventRepo.findAll().stream()
                .filter(e -> keyword == null || keyword.isBlank() || contains(e.getTitle(), keyword))
                .filter(e -> status == null || status.isBlank() || status.equalsIgnoreCase(e.getStatus()))
                .filter(e -> archived == null || archived.equals(e.isArchived()))
                .filter(e -> organizerId == null || organizerId.equals(e.getOrganizerId()))
                .map(this::mapEvent)
                .toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<Map<String, Object>>> reservations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus) {
        LocalDateTime fromDate = parseDate(from);
        LocalDateTime toDate = parseDate(to);
        List<Map<String, Object>> out = reservationRepo.findAll().stream()
                .filter(r -> userId == null || userId.equals(r.getUserId()))
                .filter(r -> eventId == null || eventId.equals(r.getEventId()))
                .filter(r -> status == null || status.isBlank() || status.equalsIgnoreCase(r.getStatus()))
                .filter(r -> paymentStatus == null || paymentStatus.isBlank() || paymentStatus.equalsIgnoreCase(r.getPaymentStatus()))
                .filter(r -> fromDate == null || (r.getReservationDate() != null && !r.getReservationDate().isBefore(fromDate)))
                .filter(r -> toDate == null || (r.getReservationDate() != null && !r.getReservationDate().isAfter(toDate)))
                .map(this::mapReservation)
                .toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<AdminTicketView>> tickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String typeTicket) {
        List<AdminTicketView> out = ticketRepo.findAll().stream()
                .filter(t -> userId == null || userId.equals(t.getUserId()))
                .filter(t -> eventId == null || eventId.equals(t.getEventId()))
                .filter(t -> statut == null || statut.isBlank() || statut.equalsIgnoreCase(t.getStatut()))
                .filter(t -> typeTicket == null || typeTicket.isBlank() || typeTicket.equalsIgnoreCase(t.getTypeTicket()))
                .toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<List<AdminFeedbackView>> feedbacks(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String status) {
        List<AdminFeedbackView> out = feedbackRepo.findAll().stream()
                .filter(f -> userId == null || userId.equals(f.getUserId()))
                .filter(f -> eventId == null || eventId.equals(f.getEventId()))
                .filter(f -> status == null || status.isBlank() || status.equalsIgnoreCase(f.getStatus()))
                .toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/reclamations")
    public ResponseEntity<List<AdminReclamationView>> reclamations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        List<AdminReclamationView> out = reclamationRepo.findAll().stream()
                .filter(r -> userId == null || userId.equals(r.getUserId()))
                .filter(r -> eventId == null || eventId.equals(r.getEventId()))
                .filter(r -> status == null || status.isBlank() || status.equalsIgnoreCase(r.getStatus()))
                .filter(r -> type == null || type.isBlank() || type.equalsIgnoreCase(r.getType()))
                .toList();
        return ResponseEntity.ok(out);
    }

    private Map<String, Object> mapEvent(AdminEventView e) {
        Map<String, Object> out = new HashMap<>();
        out.put("id", e.getId());
        out.put("title", e.getTitle());
        out.put("description", e.getDescription());
        out.put("date", e.getDate());
        out.put("place", e.getPlace());
        out.put("price", e.getPrice());
        out.put("organizerId", e.getOrganizerId());
        out.put("imageUrl", e.getImageUrl());
        out.put("nbPlaces", e.getNbPlaces());
        out.put("nbLikes", e.getNbLikes());
        out.put("domaines", parseDomaines(e.getDomainesJson()));
        out.put("status", e.getStatus());
        out.put("archived", e.isArchived());
        out.put("createdAt", e.getCreatedAt());
        out.put("updatedAt", e.getUpdatedAt());
        return out;
    }

    private Map<String, Object> mapReservation(AdminReservationView r) {
        Map<String, Object> out = new HashMap<>();
        out.put("id", r.getId());
        out.put("userId", r.getUserId());
        out.put("eventId", r.getEventId());
        out.put("ticketId", r.getTicketId());
        out.put("numberOfPlaces", r.getNumberOfPlaces());
        out.put("totalPrice", r.getTotalPrice());
        out.put("status", r.getStatus());
        out.put("paymentStatus", r.getPaymentStatus());
        out.put("reservationDate", r.getReservationDate());
        out.put("confirmationDate", r.getConfirmationDate());
        out.put("cancellationDate", r.getCancellationDate());
        out.put("cancellationReason", r.getCancellationReason());
        out.put("auditLog", parseAuditLog(r.getAuditLogJson()));
        out.put("createdAt", r.getCreatedAt());
        out.put("updatedAt", r.getUpdatedAt());
        out.put("username", r.getUsername());
        out.put("eventTitle", r.getEventTitle());
        out.put("eventDate", r.getEventDate());
        return out;
    }

    private List<String> parseDomaines(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<Map<String, Object>> parseAuditLog(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        try {
            return objectMapper.readValue(raw, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private LocalDateTime parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean contains(String value, String query) {
        return value != null && query != null && value.toLowerCase().contains(query.toLowerCase());
    }

    private String safe(String value) {
        return value == null ? "UNKNOWN" : value;
    }
}
