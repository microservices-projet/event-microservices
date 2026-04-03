package com.example.reservationservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private String place;
    private LocalDateTime date;
    private Double price;
    private Integer nbPlaces;
}
