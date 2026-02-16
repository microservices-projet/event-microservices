package org.example.eventmodule;


import jakarta.persistence.*;

import java.time.LocalDateTime;



@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String title;

    private String description;


    private LocalDateTime startDate;


    private LocalDateTime endDate;

    private String location;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private boolean archived = false;

    // 🔹 Constructor vide obligatoire pour JPA
    public Event() {}

    // 🔹 Constructor complet
    public Event(Long id, String title, String description,
                 LocalDateTime startDate, LocalDateTime endDate,
                 String location, EventStatus status, boolean archived) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.status = status;
        this.archived = archived;
    }

    // 🔹 Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}
