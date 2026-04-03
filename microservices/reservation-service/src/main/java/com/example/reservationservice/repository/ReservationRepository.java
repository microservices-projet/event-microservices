package com.example.reservationservice.repository;

import com.example.reservationservice.document.Reservation;
import com.example.reservationservice.document.ReservationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByEventId(Long eventId);
    List<Reservation> findByEventIdAndStatusIn(Long eventId, List<ReservationStatus> statuses);
    List<Reservation> findByStatus(ReservationStatus status);
}
