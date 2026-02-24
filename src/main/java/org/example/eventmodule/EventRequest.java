package org.example.eventmodule.request;

import lombok.Data;
import org.example.eventmodule.EventStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventRequest {
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
    private EventStatus status;
}