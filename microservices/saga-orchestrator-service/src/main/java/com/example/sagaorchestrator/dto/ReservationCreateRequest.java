package com.example.sagaorchestrator.dto;

import lombok.Data;

@Data
public class ReservationCreateRequest {
    private Long userId;
    private Long eventId;
    private int numberOfPlaces;
}
