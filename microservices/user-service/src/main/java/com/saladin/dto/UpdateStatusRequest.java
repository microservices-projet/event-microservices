package com.saladin.dto;

import com.saladin.entity.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull
    private Status status;
}
