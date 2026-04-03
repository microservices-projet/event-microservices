package com.example.reservationservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class TicketDTO {
    private Long idTicket;
    private Long eventId;
    private Long userId;
    private String nomClient;
    private String statut;
    private Integer nombreMaxTickets;
    private Double prix;
}
