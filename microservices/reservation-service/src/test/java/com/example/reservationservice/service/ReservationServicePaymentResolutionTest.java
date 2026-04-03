package com.example.reservationservice.service;

import com.example.reservationservice.client.EventClient;
import com.example.reservationservice.client.TicketClient;
import com.example.reservationservice.client.UserClient;
import com.example.reservationservice.document.PaymentStatus;
import com.example.reservationservice.document.Reservation;
import com.example.reservationservice.document.ReservationStatus;
import com.example.reservationservice.kafka.ReservationProducer;
import com.example.reservationservice.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServicePaymentResolutionTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationProducer reservationProducer;
    @Mock
    private UserClient userClient;
    @Mock
    private EventClient eventClient;
    @Mock
    private TicketClient ticketClient;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void paymentCompletedConfirmsReservation() {
        Reservation reservation = Reservation.builder()
                .id("res-1")
                .userId(1L)
                .eventId(1L)
                .numberOfPlaces(2)
                .status(ReservationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        when(reservationRepository.findById("res-1")).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        var response = reservationService.resolvePaymentCompleted("res-1", "pay-1");
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
    }

    @Test
    void paymentFailedMarksFailed() {
        Reservation reservation = Reservation.builder()
                .id("res-2")
                .userId(1L)
                .eventId(1L)
                .numberOfPlaces(2)
                .status(ReservationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        when(reservationRepository.findById("res-2")).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        var response = reservationService.resolvePaymentFailed("res-2", "declined");
        assertEquals(ReservationStatus.FAILED, response.getStatus());
        assertEquals(PaymentStatus.FAILED, response.getPaymentStatus());
    }
}
