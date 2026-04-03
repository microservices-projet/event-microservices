package com.example.sagaorchestrator.dto;

import lombok.Data;

@Data
public class EventSummaryResponse {
    private Long id;
    private Double price;
    private String title;
}
