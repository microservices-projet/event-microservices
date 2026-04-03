package com.example.paymentservice.service;

import com.example.eventcontract.EventEnvelopeContract;
import com.example.eventcontract.PaymentEventPayload;
import com.example.paymentservice.dto.PaymentSessionResponse;
import com.example.paymentservice.kafka.PaymentEventPublisher;
import com.example.paymentservice.model.PaymentRecord;
import com.example.paymentservice.model.ProcessedEventRecord;
import com.example.paymentservice.repository.PaymentRecordRepository;
import com.example.paymentservice.repository.ProcessedEventRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRecordRepository paymentRecordRepository;
    private final ProcessedEventRecordRepository processedEventRecordRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public Optional<PaymentSessionResponse> handleReservationCreated(
            String eventId,
            String reservationId,
            Double amount,
            String correlationId
    ) {
        if (eventId == null || processedEventRecordRepository.existsById(eventId)) {
            return Optional.empty();
        }

        PaymentRecord paymentRecord = paymentRecordRepository.findByReservationId(reservationId)
                .orElseGet(() -> createPaymentRecord(reservationId, amount));
        paymentRecord.setStatus("INITIATED");
        paymentRecord.setUpdatedAt(LocalDateTime.now());
        paymentRecordRepository.save(paymentRecord);

        processedEventRecordRepository.save(new ProcessedEventRecord(eventId, LocalDateTime.now()));

        PaymentEventPayload payload = toPayload(paymentRecord);
        paymentEventPublisher.publish(payload, EventEnvelopeContract.EVENT_PAYMENT_INITIATED, correlationId);

        return Optional.of(toSessionResponse(paymentRecord));
    }

    @Transactional
    public PaymentSessionResponse markCompleted(String paymentId, String correlationId) {
        PaymentRecord record = paymentRecordRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown paymentId: " + paymentId));
        if ("COMPLETED".equals(record.getStatus())) {
            throw new IllegalArgumentException("Payment already completed");
        }
        if (!"INITIATED".equals(record.getStatus()) && !"FAILED".equals(record.getStatus())) {
            throw new IllegalArgumentException("Cannot complete payment from status: " + record.getStatus());
        }
        record.setStatus("COMPLETED");
        record.setFailureReason(null);
        record.setUpdatedAt(LocalDateTime.now());
        paymentRecordRepository.save(record);

        PaymentEventPayload payload = toPayload(record);
        paymentEventPublisher.publish(payload, EventEnvelopeContract.EVENT_PAYMENT_COMPLETED, correlationId);
        return toSessionResponse(record);
    }

    @Transactional
    public PaymentSessionResponse markFailed(String paymentId, String failureReason, String correlationId) {
        PaymentRecord record = paymentRecordRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown paymentId: " + paymentId));
        if ("COMPLETED".equals(record.getStatus())) {
            throw new IllegalArgumentException("Cannot fail a completed payment");
        }
        if (!"INITIATED".equals(record.getStatus()) && !"FAILED".equals(record.getStatus())) {
            throw new IllegalArgumentException("Cannot mark failed from status: " + record.getStatus());
        }
        record.setStatus("FAILED");
        record.setFailureReason(failureReason);
        record.setUpdatedAt(LocalDateTime.now());
        paymentRecordRepository.save(record);

        PaymentEventPayload payload = toPayload(record);
        paymentEventPublisher.publish(payload, EventEnvelopeContract.EVENT_PAYMENT_FAILED, correlationId);
        return toSessionResponse(record);
    }

    public Optional<PaymentSessionResponse> getByReservationId(String reservationId) {
        return paymentRecordRepository.findByReservationId(reservationId).map(this::toSessionResponse);
    }

    /**
     * Resets a FAILED payment to INITIATED and re-publishes PAYMENT_INITIATED. Idempotent if already INITIATED.
     */
    @Transactional
    public PaymentSessionResponse retryPayment(String paymentId, String correlationId) {
        PaymentRecord record = paymentRecordRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown paymentId: " + paymentId));
        if ("COMPLETED".equals(record.getStatus())) {
            throw new IllegalArgumentException("Payment already completed");
        }
        if ("INITIATED".equals(record.getStatus())) {
            return toSessionResponse(record);
        }
        if (!"FAILED".equals(record.getStatus())) {
            throw new IllegalArgumentException("Retry only allowed from FAILED or INITIATED status");
        }
        record.setStatus("INITIATED");
        record.setFailureReason(null);
        record.setUpdatedAt(LocalDateTime.now());
        paymentRecordRepository.save(record);
        PaymentEventPayload payload = toPayload(record);
        paymentEventPublisher.publish(payload, EventEnvelopeContract.EVENT_PAYMENT_INITIATED, correlationId);
        return toSessionResponse(record);
    }

    private PaymentRecord createPaymentRecord(String reservationId, Double amount) {
        PaymentRecord record = new PaymentRecord();
        record.setPaymentId(UUID.randomUUID().toString());
        record.setReservationId(reservationId);
        record.setStatus("PENDING");
        record.setProvider("LOCAL_PSP");
        record.setSessionId("sess_" + UUID.randomUUID());
        record.setAmount(amount);
        record.setCurrency("TND");
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return record;
    }

    private PaymentEventPayload toPayload(PaymentRecord record) {
        PaymentEventPayload payload = new PaymentEventPayload();
        payload.setPaymentId(record.getPaymentId());
        payload.setReservationId(record.getReservationId());
        payload.setStatus(record.getStatus());
        payload.setProvider(record.getProvider());
        payload.setSessionId(record.getSessionId());
        payload.setAmount(record.getAmount());
        payload.setCurrency(record.getCurrency());
        payload.setFailureReason(record.getFailureReason());
        payload.setUpdatedAt(Instant.now());
        return payload;
    }

    private PaymentSessionResponse toSessionResponse(PaymentRecord record) {
        PaymentSessionResponse response = new PaymentSessionResponse();
        response.setPaymentId(record.getPaymentId());
        response.setReservationId(record.getReservationId());
        response.setStatus(record.getStatus());
        response.setSessionId(record.getSessionId());
        response.setAmount(record.getAmount());
        response.setCurrency(record.getCurrency());
        response.setProvider(record.getProvider());
        return response;
    }
}
