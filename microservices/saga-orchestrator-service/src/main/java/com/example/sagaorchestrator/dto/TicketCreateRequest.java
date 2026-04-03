package com.example.sagaorchestrator.dto;

import lombok.Data;

@Data
public class TicketCreateRequest {
    private Long userId;
    private Double prix;
    private Integer nombreMaxTickets;
    private String nomClient;
    private String emailClient;
    private String statut;
    private String typeTicket;
}

