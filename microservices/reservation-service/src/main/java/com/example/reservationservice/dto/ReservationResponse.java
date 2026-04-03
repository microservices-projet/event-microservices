package com.example.reservationservice.dto;

import com.example.reservationservice.document.AuditEntry;
import com.example.reservationservice.document.PaymentStatus;
import com.example.reservationservice.document.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReservationResponse {
    private String id;
    private Long userId;
    private Long eventId;
    private Long ticketId;
    private int numberOfPlaces;
    private Double totalPrice;
    private ReservationStatus status;
    private PaymentStatus paymentStatus;
    private LocalDateTime reservationDate;
    private LocalDateTime confirmationDate;
    private LocalDateTime cancellationDate;
    private String cancellationReason;
    private List<AuditEntry> auditLog;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private String eventTitle;
    private LocalDateTime eventDate;
}
