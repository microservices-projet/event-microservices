package org.example.eventmodule;

import org.example.eventmodule.Event;
import org.example.eventmodule.EventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import static org.example.eventmodule.config.RabbitMQConfig.EXCHANGE;
import static org.example.eventmodule.config.RabbitMQConfig.ROUTING_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void sendEvent(Event event, String action) {
        EventDTO eventDTO = mapToDTO(event);
        eventDTO.setAction(action);
        log.info("📤 Envoi à RabbitMQ : {} - {}", action, event.getTitle());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, eventDTO);
    }

    private EventDTO mapToDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate())
                .place(event.getPlace())
                .price(event.getPrice())
                .organizerId(event.getOrganizerId())
                .imageUrl(event.getImageUrl())
                .nbPlaces(event.getNbPlaces())
                .domaines(event.getDomaines())
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .build();
    }
}