package com.example.reservationservice.client;

import com.example.reservationservice.dto.EventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/api/events/{id}")
    EventDTO getEventById(@PathVariable("id") Long id);
}
