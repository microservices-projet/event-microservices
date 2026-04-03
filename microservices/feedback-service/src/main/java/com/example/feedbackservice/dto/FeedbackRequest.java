package com.example.feedbackservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotNull
    private Long eventId;

    @NotNull
    private Long userId;

    @Min(1)
    @Max(5)
    private int rating;

    private String comment;
}
