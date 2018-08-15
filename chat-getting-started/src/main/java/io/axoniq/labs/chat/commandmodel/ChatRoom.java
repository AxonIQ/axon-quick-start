package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.ArrayList;
import java.util.List;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class ChatRoom {
    @AggregateIdentifier
    private String roomId;
    private List<String> participants = new ArrayList<>();

    public ChatRoom() {
    }

    @CommandHandler
    public ChatRoom(CreateRoomCommand createRoomCommand) {
        apply(new RoomCreatedEvent(createRoomCommand.getRoomId(), createRoomCommand.getName()));
    }

    @CommandHandler
    public void handle(JoinRoomCommand joinRoomCommand) {
        if (!participants.contains(joinRoomCommand.getParticipant())) {
            apply(new ParticipantJoinedRoomEvent(
                    joinRoomCommand.getParticipant(),
                    joinRoomCommand.getRoomId()
            ));
        }
    }

    @CommandHandler
    public void handle(LeaveRoomCommand leaveRoomCommand) {
        if (participants.contains(leaveRoomCommand.getParticipant())) {
            apply(new ParticipantLeftRoomEvent(
                    leaveRoomCommand.getParticipant(),
                    leaveRoomCommand.getRoomId()
            ));
        }
    }

    @CommandHandler
    public void handle(PostMessageCommand postMessageCommand) {
        if (!participants.contains(postMessageCommand.getParticipant())) {
            throw new IllegalStateException("Participant may only post messages to rooms he/she has joined.");
        }
        apply(new MessagePostedEvent(
                postMessageCommand.getParticipant(),
                postMessageCommand.getRoomId(),
                postMessageCommand.getMessage()
        ));
    }

    @EventSourcingHandler
    protected void on(RoomCreatedEvent roomCreatedEvent) {
        roomId = roomCreatedEvent.getRoomId();
    }

    @EventSourcingHandler
    protected void on(ParticipantJoinedRoomEvent participantJoinedRoomEvent) {
        participants.add(participantJoinedRoomEvent.getParticipant());
    }

    @EventSourcingHandler
    protected void on(ParticipantLeftRoomEvent participantLeftRoomEvent) {
        participants.remove(participantLeftRoomEvent.getParticipant());
    }
}
