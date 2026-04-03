package com.example.reclamationservice.dto;

import lombok.Data;

@Data
public class TicketDTO {
    private Long idTicket;
    private Long eventId;
    private Long userId;
    private String nomClient;
    private String statut;
}
