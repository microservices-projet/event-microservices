package com.saladin.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_event_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEventView {
    @Id
    private Long id;
    private String title;
    @Column(length = 2000)
    private String description;
    private LocalDateTime date;
    private String place;
    private Double price;
    private Long organizerId;
    private String imageUrl;
    private Integer nbPlaces;
    private Integer nbLikes;
    @Lob
    private String domainesJson;
    private String status;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
