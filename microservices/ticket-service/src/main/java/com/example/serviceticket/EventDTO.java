package com.example.serviceticket;

import java.time.LocalDateTime;

public class EventDTO {
    private Long id;
    private String title;
    private String place;
    private Long organizerId;
    private Integer nbPlaces;
    private LocalDateTime dateDebut;  // Ajouté
    private LocalDateTime dateFin;    // Ajouté
    private String lieu;              // Ajouté
    private String description;

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }
    public String getPlace() { return place != null ? place : lieu; }
    public void setPlace(String place) { this.place = place; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Constructeurs
    public EventDTO() {}
    public EventDTO(Long id, String title, Long organizerId, Integer nbPlaces) {
        this.id = id;
        this.title = title;
        this.organizerId = organizerId;
        this.nbPlaces = nbPlaces;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
    public Integer getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(Integer nbPlaces) { this.nbPlaces = nbPlaces; }
}