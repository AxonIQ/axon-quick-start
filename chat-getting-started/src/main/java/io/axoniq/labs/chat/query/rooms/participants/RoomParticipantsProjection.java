package io.axoniq.labs.chat.query.rooms.participants;

import org.springframework.stereotype.Component;

@Component
public class RoomParticipantsProjection {

    private final RoomParticipantsRepository repository;

    public RoomParticipantsProjection(RoomParticipantsRepository repository) {
        this.repository = repository;
    }

    // TODO: Create some event handlers that update this model when necessary.

    // TODO: Create the query handler to read data from this model.
}
