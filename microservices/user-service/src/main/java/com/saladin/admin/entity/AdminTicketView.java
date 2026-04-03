package com.saladin.admin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_ticket_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTicketView {
    @Id
    private Long idTicket;
    private Long eventId;
    private Long userId;
    private String nomClient;
    private String emailClient;
    private Double prix;
    private String eventTitle;
    private String statut;
    private String typeTicket;
    private Integer nombreMaxTickets;
    private LocalDateTime dateCreation;
}
