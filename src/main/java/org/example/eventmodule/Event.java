package org.example.eventmodule;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private String place;

    @Column(nullable = false)
    private Double price;

    @Column(name = "organizer_id")
    private Long organizerId;

    @Column(length = 500)
    private String imageUrl;

    @Column(name = "nb_places")
    private Integer nbPlaces;

    @Column(name = "nb_likes")
    @Builder.Default
    private Integer nbLikes = 0;

    @ElementCollection
    @CollectionTable(name = "event_domains", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "domain")
    @Builder.Default
    private List<String> domaines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @Builder.Default
    private boolean archived = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (nbLikes == null) nbLikes = 0;
        if (domaines == null) domaines = new ArrayList<>();
        if (status == null) status = EventStatus.PUBLISHED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}