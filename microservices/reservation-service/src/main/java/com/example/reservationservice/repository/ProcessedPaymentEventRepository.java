package com.example.reservationservice.repository;

import com.example.reservationservice.document.ProcessedPaymentEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedPaymentEventRepository extends MongoRepository<ProcessedPaymentEvent, String> {
}
