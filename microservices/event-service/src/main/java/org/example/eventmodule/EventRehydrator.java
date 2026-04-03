package org.example.eventmodule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.eventmodule.outbox.DomainEventRecord;
import org.example.eventmodule.outbox.DomainEventRecordRepository;
import org.example.eventmodule.exception.EventNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventRehydrator {

    private final DomainEventRecordRepository domainEventRecordRepository;
    private final ObjectMapper objectMapper;

    public Event rehydrate(Long aggregateId) {
        String aggregateIdStr = String.valueOf(aggregateId);

        List<DomainEventRecord> history = domainEventRecordRepository
                .findByAggregateIdOrderByOccurredAtAsc(aggregateIdStr);

        if (history.isEmpty()) {
            throw new EventNotFoundException("Event not found in event history for id: " + aggregateId);
        }

        // Minimal replay strategy (hybrid migration):
        // since each stored domain event contains the full entity payload,
        // rehydration is equivalent to taking the latest snapshot.
        DomainEventRecord last = history.get(history.size() - 1);
        try {
            Event state = objectMapper.readValue(last.getPayloadJson(), Event.class);
            state.setId(aggregateId);
            return state;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to rehydrate Event from event history for id: " + aggregateId, e);
        }
    }
}

