package com.example.feedbackservice.dto;

import com.example.feedbackservice.entity.FeedbackStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModerationRequest {
    @NotNull
    private FeedbackStatus status;
    @NotNull
    private Long moderatedBy;
    private String moderationNote;
}
