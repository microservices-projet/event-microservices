package com.example.paymentservice.repository;

import com.example.paymentservice.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {
    Optional<PaymentRecord> findByReservationId(String reservationId);
}
