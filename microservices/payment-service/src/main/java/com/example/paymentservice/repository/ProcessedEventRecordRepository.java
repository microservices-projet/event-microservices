package com.example.paymentservice.repository;

import com.example.paymentservice.model.ProcessedEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRecordRepository extends JpaRepository<ProcessedEventRecord, String> {
}
