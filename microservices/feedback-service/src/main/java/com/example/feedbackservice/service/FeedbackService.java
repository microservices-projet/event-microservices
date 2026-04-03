package com.example.feedbackservice.service;

import com.example.feedbackservice.client.EventClient;
import com.example.feedbackservice.client.UserClient;
import com.example.feedbackservice.dto.*;
import com.example.feedbackservice.entity.Feedback;
import com.example.feedbackservice.entity.FeedbackStatus;
import com.example.feedbackservice.kafka.FeedbackProducer;
import com.example.feedbackservice.repository.FeedbackRepository;
import com.example.feedbackservice.util.ContentFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackProducer feedbackProducer;
    private final UserClient userClient;
    private final EventClient eventClient;

    public FeedbackResponse create(FeedbackRequest request) {
        if (feedbackRepository.existsByUserIdAndEventId(request.getUserId(), request.getEventId())) {
            throw new RuntimeException("Vous avez déjà soumis un feedback pour cet événement");
        }

        Feedback feedback = Feedback.builder()
                .eventId(request.getEventId())
                .userId(request.getUserId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        if (ContentFilter.containsInappropriateContent(request.getComment())) {
            feedback.setStatus(FeedbackStatus.FLAGGED);
            feedback.setFlaggedReason(ContentFilter.getFlagReason(request.getComment()));
            log.warn("Feedback auto-flagged for user={} event={}", request.getUserId(), request.getEventId());
        } else {
            feedback.setStatus(FeedbackStatus.PENDING);
        }

        Feedback saved = feedbackRepository.save(feedback);
        feedbackProducer.sendFeedbackCreated(saved);
        return mapToResponse(saved);
    }

    public List<FeedbackResponse> getAll() {
        return feedbackRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FeedbackResponse getById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback non trouvé : " + id));
        return mapToResponse(feedback);
    }

    public List<FeedbackResponse> getByEventId(Long eventId) {
        return feedbackRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FeedbackResponse> getByUserId(Long userId) {
        return feedbackRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FeedbackStatsDTO getStatsByEventId(Long eventId) {
        List<Feedback> feedbacks = feedbackRepository.findByEventIdAndStatus(eventId, FeedbackStatus.APPROVED);

        double avg = feedbacks.stream().mapToInt(Feedback::getRating).average().orElse(0.0);
        Map<Integer, Long> distribution = feedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting()));

        return FeedbackStatsDTO.builder()
                .eventId(eventId)
                .averageRating(Math.round(avg * 100.0) / 100.0)
                .totalFeedbacks(feedbacks.size())
                .ratingDistribution(distribution)
                .build();
    }

    public FeedbackResponse moderate(Long id, ModerationRequest request) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback non trouvé : " + id));

        feedback.setStatus(request.getStatus());
        feedback.setModeratedBy(request.getModeratedBy());
        feedback.setModerationNote(request.getModerationNote());

        Feedback saved = feedbackRepository.save(feedback);
        feedbackProducer.sendFeedbackModerated(saved);
        return mapToResponse(saved);
    }

    public void delete(Long id) {
        if (!feedbackRepository.existsById(id)) {
            throw new RuntimeException("Feedback non trouvé : " + id);
        }
        feedbackRepository.deleteById(id);
        feedbackProducer.sendFeedbackDeleted(id);
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        FeedbackResponse.FeedbackResponseBuilder builder = FeedbackResponse.builder()
                .id(feedback.getId())
                .eventId(feedback.getEventId())
                .userId(feedback.getUserId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .status(feedback.getStatus())
                .moderatedBy(feedback.getModeratedBy())
                .moderationNote(feedback.getModerationNote())
                .flaggedReason(feedback.getFlaggedReason())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt());

        try {
            UserDTO user = userClient.getUserById(feedback.getUserId());
            builder.username(user.getUsername());
        } catch (Exception e) {
            log.warn("Could not fetch user {}: {}", feedback.getUserId(), e.getMessage());
        }

        try {
            EventDTO event = eventClient.getEventById(feedback.getEventId());
            builder.eventTitle(event.getTitle());
        } catch (Exception e) {
            log.warn("Could not fetch event {}: {}", feedback.getEventId(), e.getMessage());
        }

        return builder.build();
    }
}
