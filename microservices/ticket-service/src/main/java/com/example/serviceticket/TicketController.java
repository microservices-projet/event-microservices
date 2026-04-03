package com.example.serviceticket;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;
    private final EventClient eventClient;

    public TicketController(TicketService ticketService, EventClient eventClient) {
        this.ticketService = ticketService;
        this.eventClient = eventClient;
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket, @PathVariable Long eventId) {
        try {
            Ticket created = ticketService.createTicket(ticket, eventId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Erreur creation billet"));
        }
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getTicketsByEvent(@PathVariable Long eventId) {
        try {
            eventClient.getEventById(eventId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Événement inexistant avec l'ID: " + eventId);
        }
        return ResponseEntity.ok(ticketService.getTicketsByEventId(eventId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsByUserId(userId));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<Ticket> updateStatut(@PathVariable Long id, @RequestParam StatusTicket statut) {
        return ticketService.updateTicketStatut(id, statut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/type")
    public ResponseEntity<Ticket> updateType(@PathVariable Long id, @RequestParam TypeTicket typeTicket) {
        return ticketService.updateTicketType(id, typeTicket)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable Long id, @RequestBody Ticket ticket) {
        try {
            return ResponseEntity.ok(ticketService.updateTicket(id, ticket));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
