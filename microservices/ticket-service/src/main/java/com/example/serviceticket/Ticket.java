package com.example.serviceticket;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTicket;

    private Long eventId;
    private Long userId;

    private String nomClient;
    private String emailClient;
    private Double prix;
    private String eventTitle;

    @Enumerated(EnumType.STRING)
    private StatusTicket statut;

    @Enumerated(EnumType.STRING)
    private TypeTicket typeTicket;

    private Integer nombreMaxTickets;

    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
    }
}
