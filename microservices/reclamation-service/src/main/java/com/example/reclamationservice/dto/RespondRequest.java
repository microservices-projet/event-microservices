package com.example.reclamationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RespondRequest {
    @NotBlank
    private String response;
}
