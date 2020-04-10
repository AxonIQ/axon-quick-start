package io.axoniq.labs.chat.query.rooms.summary;

import org.springframework.stereotype.Component;

@Component
public class RoomSummaryProjection {

    private final RoomSummaryRepository roomSummaryRepository;

    public RoomSummaryProjection(RoomSummaryRepository roomSummaryRepository) {
        this.roomSummaryRepository = roomSummaryRepository;
    }

    // TODO: Create some event handlers that update this model when necessary.

    // TODO: Create the query handler to read data from this model.
}
