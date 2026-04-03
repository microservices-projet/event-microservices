package com.example.sagaorchestrator.dto;

import lombok.Data;

@Data
public class CancelRequest {
    private String reason;
    private Long cancelledBy;
}

