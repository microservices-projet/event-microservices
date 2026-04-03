package com.example.reclamationservice.dto;

import com.example.reclamationservice.entity.ReclamationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReclamationRequest {

    @NotNull
    private Long userId;

    private Long eventId;
    private String reservationId;
    private Long ticketId;

    @NotBlank
    private String subject;

    @NotBlank
    private String description;

    private ReclamationType type;
}
