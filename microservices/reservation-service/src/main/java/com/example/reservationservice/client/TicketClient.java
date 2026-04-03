package com.example.reservationservice.client;

import com.example.reservationservice.dto.TicketDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ticket-service")
public interface TicketClient {

    @GetMapping("/api/tickets/event/{eventId}")
    List<TicketDTO> getTicketsByEvent(@PathVariable("eventId") Long eventId);
}
