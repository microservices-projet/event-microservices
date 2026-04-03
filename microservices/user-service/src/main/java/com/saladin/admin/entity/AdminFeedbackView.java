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
@Table(name = "admin_feedback_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFeedbackView {
    @Id
    private Long id;
    private Long eventId;
    private Long userId;
    private Integer rating;
    @Column(length = 2000)
    private String comment;
    private String status;
    private Long moderatedBy;
    @Column(length = 1000)
    private String moderationNote;
    @Column(length = 500)
    private String flaggedReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private String eventTitle;
}
