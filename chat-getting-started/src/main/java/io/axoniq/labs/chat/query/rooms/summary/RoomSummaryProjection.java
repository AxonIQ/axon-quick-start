package io.axoniq.labs.chat.query.rooms.summary;

import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.RoomCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
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

    @EventHandler
    public void on(RoomCreatedEvent roomCreatedEvent) {
        roomSummaryRepository.save(new RoomSummary(
                roomCreatedEvent.getRoomId(),
                roomCreatedEvent.getName()
        ));
    }

    @EventHandler
    public void on(ParticipantJoinedRoomEvent participantJoinedRoomEvent) {
        RoomSummary roomSummary = roomSummaryRepository.findOne(participantJoinedRoomEvent.getRoomId());
        roomSummary.addParticipant();
    }

    @EventHandler
    public void on(ParticipantLeftRoomEvent participantLeftRoomEvent) {
        RoomSummary roomSummary = roomSummaryRepository.findOne(participantLeftRoomEvent.getRoomId());
        roomSummary.removeParticipant();
    }
}
