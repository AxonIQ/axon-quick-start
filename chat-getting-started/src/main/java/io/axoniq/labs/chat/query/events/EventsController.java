package io.axoniq.labs.chat.query.events;

import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
public class EventsController {

    private final EventStore eventStore;

    public EventsController(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @GetMapping
    @RequestMapping("/{aggregateId}")
    @Transactional(readOnly = true)
    public List<EventMessage> listEvents(@PathVariable String aggregateId) {
        return eventStore.readEvents(aggregateId).asStream().collect(Collectors.toList());
    }
}
