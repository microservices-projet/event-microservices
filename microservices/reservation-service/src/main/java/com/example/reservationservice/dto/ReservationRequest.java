package com.example.reservationservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long eventId;

    @Min(1)
    private int numberOfPlaces;
}
