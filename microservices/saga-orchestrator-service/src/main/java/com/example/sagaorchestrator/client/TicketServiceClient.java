package com.example.sagaorchestrator.client;

import com.example.sagaorchestrator.dto.TicketCreateRequest;
import com.example.sagaorchestrator.dto.TicketResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ticket-service")
public interface TicketServiceClient {

    @PostMapping("/api/tickets/event/{eventId}")
    ResponseEntity<TicketResponse> createTicket(
            @PathVariable("eventId") Long eventId,
            @RequestBody TicketCreateRequest ticket
    );

    @DeleteMapping("/api/tickets/{id}")
    ResponseEntity<Void> deleteTicket(@PathVariable("id") Long id);
}

