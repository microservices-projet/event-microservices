package com.example.serviceticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventClient eventClient;
    private final UserInternalClient userInternalClient;
    private final TicketProducer ticketProducer;

    @Transactional
    public Ticket createTicket(Ticket ticket, Long eventId) {
        EventDTO event = eventClient.getEventById(eventId);
        if (event == null) {
            throw new RuntimeException("Événement inexistant avec l'ID: " + eventId);
        }
        ticket.setEventId(eventId);
        ticket.setEventTitle(event.getTitle());
        if (ticket.getUserId() != null) {
            try {
                userInternalClient.getUserById(ticket.getUserId());
            } catch (Exception e) {
                log.warn("User not found: {}", ticket.getUserId());
                throw new RuntimeException("Utilisateur inexistant avec l'ID: " + ticket.getUserId());
            }
        }
        if (ticket.getDateCreation() == null) ticket.setDateCreation(LocalDateTime.now());
        if (ticket.getStatut() == null) ticket.setStatut(StatusTicket.DISPONIBLE);
        Ticket saved = ticketRepository.save(ticket);
        ticketProducer.sendTicketCreated(saved);
        return saved;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> getTicketsByEventId(Long eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    public List<Ticket> getTicketsByUserId(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    public Optional<Ticket> updateTicketStatut(Long id, StatusTicket statut) {
        Optional<Ticket> opt = ticketRepository.findById(id);
        opt.ifPresent(t -> {
            t.setStatut(statut);
            ticketRepository.save(t);
            ticketProducer.sendTicketUpdated(t);
        });
        return opt;
    }

    public Optional<Ticket> updateTicketType(Long id, TypeTicket typeTicket) {
        Optional<Ticket> opt = ticketRepository.findById(id);
        opt.ifPresent(t -> {
            t.setTypeTicket(typeTicket);
            ticketRepository.save(t);
            ticketProducer.sendTicketUpdated(t);
        });
        return opt;
    }

    @Transactional
    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
        ticketProducer.sendTicketDeleted(id);
    }

    @Transactional
    public Ticket updateTicket(Long id, Ticket ticketDetails) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket introuvable avec l'ID : " + id));
        if (ticketDetails.getEmailClient() != null) ticket.setEmailClient(ticketDetails.getEmailClient());
        if (ticketDetails.getNomClient() != null) ticket.setNomClient(ticketDetails.getNomClient());
        if (ticketDetails.getNombreMaxTickets() != null) ticket.setNombreMaxTickets(ticketDetails.getNombreMaxTickets());
        if (ticketDetails.getPrix() != null) ticket.setPrix(ticketDetails.getPrix());
        if (ticketDetails.getStatut() != null) ticket.setStatut(ticketDetails.getStatut());
        if (ticketDetails.getTypeTicket() != null) ticket.setTypeTicket(ticketDetails.getTypeTicket());
        Ticket updated = ticketRepository.save(ticket);
        ticketProducer.sendTicketUpdated(updated);
        return updated;
    }
}
