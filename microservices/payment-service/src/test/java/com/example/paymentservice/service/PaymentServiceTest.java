package com.example.paymentservice.service;

import com.example.eventcontract.EventEnvelopeContract;
import com.example.paymentservice.dto.PaymentSessionResponse;
import com.example.paymentservice.kafka.PaymentEventPublisher;
import com.example.paymentservice.model.PaymentRecord;
import com.example.paymentservice.repository.PaymentRecordRepository;
import com.example.paymentservice.repository.ProcessedEventRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRecordRepository paymentRecordRepository;
    @Mock
    private ProcessedEventRecordRepository processedEventRecordRepository;
    @Mock
    private PaymentEventPublisher paymentEventPublisher;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createsSessionAndCompletesPayment() {
        when(processedEventRecordRepository.existsById("evt-1")).thenReturn(false);
        when(paymentRecordRepository.findByReservationId("res-1")).thenReturn(Optional.empty());
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(i -> i.getArgument(0));
        Optional<PaymentSessionResponse> created = paymentService.handleReservationCreated(
                "evt-1",
                "res-1",
                120.0,
                "corr-1"
        );
        assertTrue(created.isPresent());
        assertEquals("INITIATED", created.get().getStatus());
        assertEquals(120.0, created.get().getAmount());
        assertEquals("TND", created.get().getCurrency());
        assertEquals("LOCAL_PSP", created.get().getProvider());

        PaymentRecord existing = new PaymentRecord();
        existing.setPaymentId(created.get().getPaymentId());
        existing.setReservationId("res-1");
        existing.setStatus("INITIATED");
        when(paymentRecordRepository.findById(created.get().getPaymentId())).thenReturn(Optional.of(existing));
        PaymentSessionResponse completed = paymentService.markCompleted(created.get().getPaymentId(), "corr-1");
        assertEquals("COMPLETED", completed.getStatus());
    }

    @Test
    void markCompletedTwiceThrows() {
        PaymentRecord record = new PaymentRecord();
        record.setPaymentId("pay-1");
        record.setStatus("COMPLETED");
        when(paymentRecordRepository.findById("pay-1")).thenReturn(Optional.of(record));
        assertThrows(IllegalArgumentException.class, () -> paymentService.markCompleted("pay-1", "c1"));
    }

    @Test
    void retryFromFailedResetsToInitiatedAndPublishes() {
        PaymentRecord failed = new PaymentRecord();
        failed.setPaymentId("pay-2");
        failed.setReservationId("res-2");
        failed.setStatus("FAILED");
        failed.setSessionId("sess_x");
        failed.setProvider("LOCAL_PSP");
        failed.setAmount(50.0);
        failed.setCurrency("TND");
        when(paymentRecordRepository.findById("pay-2")).thenReturn(Optional.of(failed));
        when(paymentRecordRepository.save(any(PaymentRecord.class))).thenAnswer(i -> i.getArgument(0));

        PaymentSessionResponse after = paymentService.retryPayment("pay-2", "corr-r");

        assertEquals("INITIATED", after.getStatus());
        verify(paymentEventPublisher).publish(any(), eq(EventEnvelopeContract.EVENT_PAYMENT_INITIATED), eq("corr-r"));
    }

    @Test
    void retryWhenAlreadyInitiatedIsIdempotent() {
        PaymentRecord record = new PaymentRecord();
        record.setPaymentId("pay-3");
        record.setStatus("INITIATED");
        when(paymentRecordRepository.findById("pay-3")).thenReturn(Optional.of(record));

        PaymentSessionResponse after = paymentService.retryPayment("pay-3", "c");

        assertEquals("INITIATED", after.getStatus());
    }
}
