package com.saladin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserValidationRequest {
    @NotNull
    private Long userId;

    private String token;
}
