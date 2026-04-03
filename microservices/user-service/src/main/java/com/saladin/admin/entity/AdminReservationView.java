package com.saladin.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_reservation_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReservationView {
    @Id
    private String id;
    private Long userId;
    private Long eventId;
    private Long ticketId;
    private Integer numberOfPlaces;
    private Double totalPrice;
    private String status;
    private String paymentStatus;
    private LocalDateTime reservationDate;
    private LocalDateTime confirmationDate;
    private LocalDateTime cancellationDate;
    @Column(length = 1000)
    private String cancellationReason;
    @Lob
    private String auditLogJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private String eventTitle;
    private LocalDateTime eventDate;
}
