package com.example.sagaorchestrator.client;

import com.example.sagaorchestrator.dto.CancelRequest;
import com.example.sagaorchestrator.dto.ReservationCreateRequest;
import com.example.sagaorchestrator.dto.ReservationSummaryResponse;
import com.example.sagaorchestrator.dto.TicketLinkRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reservation-service")
public interface ReservationServiceClient {

    @PostMapping("/api/reservations")
    ResponseEntity<ReservationSummaryResponse> create(@RequestBody ReservationCreateRequest body);

    @PatchMapping("/api/reservations/{id}/ticket")
    ResponseEntity<ReservationSummaryResponse> attachTicket(
            @PathVariable("id") String id,
            @RequestBody TicketLinkRequest body
    );

    @PatchMapping("/api/reservations/{id}/cancel")
    ResponseEntity<Object> cancel(
            @PathVariable("id") String id,
            @RequestBody CancelRequest request
    );
}
