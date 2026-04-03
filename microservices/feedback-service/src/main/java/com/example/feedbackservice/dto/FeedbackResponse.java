package com.example.feedbackservice.dto;

import com.example.feedbackservice.entity.FeedbackStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private Long id;
    private Long eventId;
    private Long userId;
    private int rating;
    private String comment;
    private FeedbackStatus status;
    private Long moderatedBy;
    private String moderationNote;
    private String flaggedReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private String eventTitle;
}
