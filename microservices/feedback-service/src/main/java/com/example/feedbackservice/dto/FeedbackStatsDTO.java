package com.example.feedbackservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class FeedbackStatsDTO {
    private Long eventId;
    private double averageRating;
    private long totalFeedbacks;
    private Map<Integer, Long> ratingDistribution;
}
