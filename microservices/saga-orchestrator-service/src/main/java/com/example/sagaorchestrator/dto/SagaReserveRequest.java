package com.example.sagaorchestrator.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SagaReserveRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long eventId;

    @Positive
    private int places = 1;

    /** Optional; when set, replays return the same outcome without duplicating side effects */
    private String idempotencyKey;

    private String nomClient;
    private String emailClient;
}
