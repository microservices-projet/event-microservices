package com.example.sagaorchestrator.dto;

import lombok.Data;

@Data
public class TicketResponse {
    private Long idTicket;
    private Long eventId;
    private Long userId;
}
