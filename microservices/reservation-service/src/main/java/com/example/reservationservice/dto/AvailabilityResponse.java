package com.example.reservationservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailabilityResponse {
    private Long eventId;
    private int totalPlaces;
    private int reservedPlaces;
    private int availablePlaces;
}
