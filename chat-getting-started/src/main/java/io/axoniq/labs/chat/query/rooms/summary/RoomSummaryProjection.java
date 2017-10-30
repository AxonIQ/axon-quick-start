package io.axoniq.labs.chat.query.rooms.summary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomSummaryProjection {

    private final RoomSummaryRepository roomSummaryRepository;

    public RoomSummaryProjection(RoomSummaryRepository roomSummaryRepository) {
        this.roomSummaryRepository = roomSummaryRepository;
    }

    @GetMapping
    public List<RoomSummary> listRooms() {
        return roomSummaryRepository.findAll();
    }

    // TODO: Create some event handlers that update this model when necessary

}
