package com.example.reclamationservice.repository;

import com.example.reclamationservice.entity.Reclamation;
import com.example.reclamationservice.entity.ReclamationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    List<Reclamation> findByUserId(Long userId);
    List<Reclamation> findByEventId(Long eventId);
    List<Reclamation> findByStatus(ReclamationStatus status);
    List<Reclamation> findByAssignedTo(Long assignedTo);
}
