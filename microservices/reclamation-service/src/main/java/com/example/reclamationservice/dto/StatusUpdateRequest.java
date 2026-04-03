package com.example.reclamationservice.dto;

import com.example.reclamationservice.entity.ReclamationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull
    private ReclamationStatus status;
}
