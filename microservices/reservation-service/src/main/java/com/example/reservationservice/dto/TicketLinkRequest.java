package com.example.reservationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketLinkRequest {

    @NotNull
    private Long ticketId;
}
