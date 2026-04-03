package com.example.reservationservice.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    private String id;

    private Long userId;
    private Long eventId;
    private Long ticketId;

    private int numberOfPlaces;
    private Double totalPrice;

    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private LocalDateTime reservationDate;
    private LocalDateTime confirmationDate;
    private LocalDateTime cancellationDate;
    private String cancellationReason;

    @Builder.Default
    private List<AuditEntry> auditLog = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void addAuditEntry(String action, Long performedBy, String details) {
        if (auditLog == null) auditLog = new ArrayList<>();
        auditLog.add(AuditEntry.builder()
                .action(action)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build());
    }
}
