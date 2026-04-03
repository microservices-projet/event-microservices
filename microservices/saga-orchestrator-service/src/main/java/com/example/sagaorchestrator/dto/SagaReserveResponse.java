package com.example.sagaorchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaReserveResponse {
    private String sagaId;
    private String status;
    /** Mongo reservation id when the distributed flow created a reservation */
    private String reservationId;
    private Long ticketId;
    private String message;
}
