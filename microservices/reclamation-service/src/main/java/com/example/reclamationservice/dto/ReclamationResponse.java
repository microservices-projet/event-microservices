package com.example.reclamationservice.dto;

import com.example.reclamationservice.entity.ReclamationPriority;
import com.example.reclamationservice.entity.ReclamationStatus;
import com.example.reclamationservice.entity.ReclamationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReclamationResponse {
    private Long id;
    private Long userId;
    private Long eventId;
    private String reservationId;
    private Long ticketId;
    private String subject;
    private String description;
    private ReclamationType type;
    private ReclamationStatus status;
    private ReclamationPriority priority;
    private Long assignedTo;
    private String response;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private String eventTitle;
    private String ticketLabel;
    private String reservationLabel;
    private String statusLabel;
    private String priorityLabel;
    private boolean canAssign;
    private boolean canRespond;
    private boolean isResolved;
}
