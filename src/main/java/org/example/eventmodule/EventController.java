package org.example.eventmodule;

import org.example.eventmodule.Event;
import org.example.eventmodule.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @PostMapping
    public Event create(@RequestBody Event event) {
        return service.create(event);
    }

    @GetMapping
    public List<Event> getAll() {
        return service.getAll();
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable Long id,
                        @RequestBody Event event) {
        return service.update(id, event);
    }

    @DeleteMapping("/{id}")
    public void archive(@PathVariable Long id) {
        service.archive(id);
    }
}
