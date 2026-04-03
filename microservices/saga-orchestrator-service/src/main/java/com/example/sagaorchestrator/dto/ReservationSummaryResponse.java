package com.example.sagaorchestrator.dto;

import lombok.Data;

@Data
public class ReservationSummaryResponse {
    private String id;
    private Long userId;
    private Double totalPrice;
    private Long ticketId;
}
