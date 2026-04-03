package com.saladin.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime date;
    private String place;
    private Double price;
    private Long organizerId;
    private String imageUrl;
    private Integer nbPlaces;
    private Integer nbLikes;
    private List<String> domaines;
    private String status;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}