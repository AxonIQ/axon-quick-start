package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.eventsourcing.EventSourcingHandler;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
    @AggregateIdentifier
    private String roomId;
    private List<String> participants = new ArrayList<>();

    public ChatRoom() {
    }

    @CommandHandler
    public ChatRoom(CreateRoomCommand createRoomCommand) {
        AggregateLifecycle.apply(new RoomCreatedEvent(createRoomCommand.getRoomId(), createRoomCommand.getName()));
    }

    @EventSourcingHandler
    public void on(RoomCreatedEvent roomCreatedEvent) {
        roomId = roomCreatedEvent.getRoomId();
    }

    @CommandHandler
    public void handle(JoinRoomCommand joinRoomCommand) {
        if (!participants.contains(joinRoomCommand.getParticipant())) {
            AggregateLifecycle.apply(new ParticipantJoinedRoomEvent(
                    joinRoomCommand.getParticipant(),
                    joinRoomCommand.getRoomId()
            ));
        }
    }

    @EventSourcingHandler
    public void on(ParticipantJoinedRoomEvent participantJoinedRoomEvent) {
        participants.add(participantJoinedRoomEvent.getParticipant());
    }

    @CommandHandler
    public void handle(LeaveRoomCommand leaveRoomCommand) {
        if (participants.contains(leaveRoomCommand.getParticipant())) {
            AggregateLifecycle.apply(new ParticipantLeftRoomEvent(
                    leaveRoomCommand.getParticipant(),
                    leaveRoomCommand.getRoomId()
            ));
        }
    }

    @EventSourcingHandler
    public void on(ParticipantLeftRoomEvent participantLeftRoomEvent) {
        participants.remove(participantLeftRoomEvent.getParticipant());
    }

    @CommandHandler
    public void handle(PostMessageCommand postMessageCommand) {
        if (!participants.contains(postMessageCommand.getParticipant())) {
            throw new IllegalStateException("Participant may only post messages to rooms he/she has joined.");
        }
        AggregateLifecycle.apply(new MessagePostedEvent(
                postMessageCommand.getParticipant(),
                postMessageCommand.getRoomId(),
                postMessageCommand.getMessage()
        ));
    }
}
