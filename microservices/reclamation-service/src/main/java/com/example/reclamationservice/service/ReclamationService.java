package com.example.reclamationservice.service;

import com.example.reclamationservice.dto.*;
import com.example.reclamationservice.entity.Reclamation;
import com.example.reclamationservice.entity.ReclamationStatus;
import com.example.reclamationservice.entity.ReclamationType;
import com.example.reclamationservice.kafka.ReferenceProjectionStore;
import com.example.reclamationservice.kafka.ReclamationProducer;
import com.example.reclamationservice.repository.ReclamationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final ReclamationProducer reclamationProducer;
    private final ReferenceProjectionStore projectionStore;

    public ReclamationResponse create(ReclamationRequest request) {
        ReclamationType type = autoClassify(request);

        Reclamation reclamation = Reclamation.builder()
                .userId(request.getUserId())
                .eventId(request.getEventId())
                .reservationId(request.getReservationId())
                .ticketId(request.getTicketId())
                .subject(request.getSubject())
                .description(request.getDescription())
                .type(type)
                .build();

        Reclamation saved = reclamationRepository.save(reclamation);
        reclamationProducer.sendReclamationCreated(saved);
        return mapToResponse(saved);
    }

    public List<ReclamationResponse> getAll() {
        return reclamationRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public ReclamationResponse getById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    public List<ReclamationResponse> getByUserId(Long userId) {
        return reclamationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ReclamationResponse> getByEventId(Long eventId) {
        return reclamationRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ReclamationResponse> getByStatus(ReclamationStatus status) {
        return reclamationRepository.findByStatus(status).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public ReclamationResponse assign(Long id, AssignRequest request) {
        Reclamation rec = findOrThrow(id);
        rec.setAssignedTo(request.getAssignedTo());
        rec.setStatus(ReclamationStatus.IN_PROGRESS);
        Reclamation saved = reclamationRepository.save(rec);
        reclamationProducer.sendReclamationUpdated(saved);
        return mapToResponse(saved);
    }

    public ReclamationResponse respond(Long id, RespondRequest request) {
        Reclamation rec = findOrThrow(id);
        rec.setResponse(request.getResponse());
        rec.setStatus(ReclamationStatus.RESOLVED);
        rec.setResolvedAt(LocalDateTime.now());
        Reclamation saved = reclamationRepository.save(rec);
        reclamationProducer.sendReclamationUpdated(saved);
        return mapToResponse(saved);
    }

    public ReclamationResponse updateStatus(Long id, StatusUpdateRequest request) {
        Reclamation rec = findOrThrow(id);
        rec.setStatus(request.getStatus());
        if (request.getStatus() == ReclamationStatus.RESOLVED || request.getStatus() == ReclamationStatus.CLOSED) {
            rec.setResolvedAt(LocalDateTime.now());
        }
        Reclamation saved = reclamationRepository.save(rec);
        reclamationProducer.sendReclamationUpdated(saved);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        if (!reclamationRepository.existsById(id)) {
            throw new RuntimeException("Réclamation non trouvée : " + id);
        }
        reclamationRepository.deleteById(id);
        reclamationProducer.sendReclamationDeleted(id);
    }

    private ReclamationType autoClassify(ReclamationRequest request) {
        if (request.getType() != null) return request.getType();
        if (request.getTicketId() != null) return ReclamationType.TICKET_ISSUE;
        if (request.getReservationId() != null) return ReclamationType.RESERVATION_ISSUE;
        if (request.getEventId() != null) return ReclamationType.EVENT_ISSUE;
        return ReclamationType.OTHER;
    }

    private Reclamation findOrThrow(Long id) {
        return reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation non trouvée : " + id));
    }

    private ReclamationResponse mapToResponse(Reclamation rec) {
        ReclamationResponse.ReclamationResponseBuilder builder = ReclamationResponse.builder()
                .id(rec.getId())
                .userId(rec.getUserId())
                .eventId(rec.getEventId())
                .reservationId(rec.getReservationId())
                .ticketId(rec.getTicketId())
                .subject(rec.getSubject())
                .description(rec.getDescription())
                .type(rec.getType())
                .status(rec.getStatus())
                .priority(rec.getPriority())
                .assignedTo(rec.getAssignedTo())
                .response(rec.getResponse())
                .resolvedAt(rec.getResolvedAt())
                .createdAt(rec.getCreatedAt())
                .updatedAt(rec.getUpdatedAt())
                .statusLabel(rec.getStatus().name().replace('_', ' '))
                .priorityLabel(rec.getPriority().name())
                .canAssign(rec.getStatus() == ReclamationStatus.OPEN)
                .canRespond(rec.getStatus() == ReclamationStatus.IN_PROGRESS)
                .isResolved(rec.getStatus() == ReclamationStatus.RESOLVED || rec.getStatus() == ReclamationStatus.CLOSED);

        projectionStore.username(rec.getUserId()).ifPresent(builder::username);
        projectionStore.eventTitle(rec.getEventId()).ifPresent(builder::eventTitle);
        projectionStore.ticketLabel(rec.getTicketId()).ifPresent(builder::ticketLabel);
        projectionStore.reservationLabel(rec.getReservationId()).ifPresent(builder::reservationLabel);

        return builder.build();
    }
}
