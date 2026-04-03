package com.example.reclamationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRequest {
    @NotNull
    private Long assignedTo;
}
