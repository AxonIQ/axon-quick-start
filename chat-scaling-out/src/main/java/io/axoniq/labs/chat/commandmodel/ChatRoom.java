package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Profile("command")
@Aggregate
public class ChatRoom {

    @AggregateIdentifier
    private String roomId;
    private Set<String> participants;

    public ChatRoom() {
    }

    @CommandHandler
    public ChatRoom(CreateRoomCommand command) {
        apply(new RoomCreatedEvent(command.getRoomId(), command.getName()));
    }

    @CommandHandler
    public void handle(JoinRoomCommand command) {
        if (!participants.contains(command.getParticipant())) {
            apply(new ParticipantJoinedRoomEvent(command.getParticipant(), roomId));
        }
    }

    @CommandHandler
    public void handle(LeaveRoomCommand command) {
        if (participants.contains(command.getParticipant())) {
            apply(new ParticipantLeftRoomEvent(command.getParticipant(), roomId));
        }
    }

    @CommandHandler
    public void handle(PostMessageCommand command) {
        Assert.state(participants.contains(command.getParticipant()),
                     "You cannot post messages unless you've joined the chat room");
        apply(new MessagePostedEvent(command.getParticipant(), roomId, command.getMessage()));
    }

    @EventSourcingHandler
    protected void on(RoomCreatedEvent event) {
        this.roomId = event.getRoomId();
        this.participants = new HashSet<>();
    }

    @EventSourcingHandler
    protected void on(ParticipantJoinedRoomEvent event) {
        this.participants.add(event.getParticipant());
    }

    @EventSourcingHandler
    protected void on(ParticipantLeftRoomEvent event) {
        this.participants.remove(event.getParticipant());
    }
}
