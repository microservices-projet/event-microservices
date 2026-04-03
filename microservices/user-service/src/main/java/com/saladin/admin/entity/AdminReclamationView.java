package com.saladin.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_reclamation_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReclamationView {
    @Id
    private Long id;
    private Long userId;
    private Long eventId;
    private String reservationId;
    private Long ticketId;
    private String subject;
    @Column(length = 3000)
    private String description;
    private String type;
    private String status;
    private String priority;
    private Long assignedTo;
    @Column(length = 3000)
    private String response;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private String eventTitle;
}
