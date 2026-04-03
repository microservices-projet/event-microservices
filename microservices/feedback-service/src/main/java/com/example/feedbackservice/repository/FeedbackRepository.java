package com.example.feedbackservice.repository;

import com.example.feedbackservice.entity.Feedback;
import com.example.feedbackservice.entity.FeedbackStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByEventId(Long eventId);
    List<Feedback> findByUserId(Long userId);
    List<Feedback> findByEventIdAndStatus(Long eventId, FeedbackStatus status);
    List<Feedback> findByStatus(FeedbackStatus status);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
