package com.example.feedbackservice.controller;

import com.example.feedbackservice.dto.*;
import com.example.feedbackservice.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAll() {
        return ResponseEntity.ok(feedbackService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getById(id));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<FeedbackResponse>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(feedbackService.getByEventId(eventId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(feedbackService.getByUserId(userId));
    }

    @GetMapping("/stats/event/{eventId}")
    public ResponseEntity<FeedbackStatsDTO> getStatsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(feedbackService.getStatsByEventId(eventId));
    }

    @PatchMapping("/{id}/moderate")
    public ResponseEntity<FeedbackResponse> moderate(
            @PathVariable Long id,
            @Valid @RequestBody ModerationRequest request) {
        return ResponseEntity.ok(feedbackService.moderate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        feedbackService.delete(id);
        return ResponseEntity.ok("Feedback supprimé");
    }
}
