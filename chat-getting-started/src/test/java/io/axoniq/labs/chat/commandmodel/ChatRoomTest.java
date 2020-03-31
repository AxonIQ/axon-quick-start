package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.CreateRoomCommand;
import io.axoniq.labs.chat.coreapi.JoinRoomCommand;
import io.axoniq.labs.chat.coreapi.LeaveRoomCommand;
import io.axoniq.labs.chat.coreapi.MessagePostedEvent;
import io.axoniq.labs.chat.coreapi.ParticipantJoinedRoomEvent;
import io.axoniq.labs.chat.coreapi.ParticipantLeftRoomEvent;
import io.axoniq.labs.chat.coreapi.PostMessageCommand;
import io.axoniq.labs.chat.coreapi.RoomCreatedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.*;

class ChatRoomTest {

    private AggregateTestFixture<ChatRoom> testFixture;

    @BeforeEach
    void setUp() {
        testFixture = new AggregateTestFixture<>(ChatRoom.class);
    }

    @Test
    void testCreateChatRoom() {
        testFixture.givenNoPriorActivity()
                   .when(new CreateRoomCommand("roomId", "test-room"))
                   .expectEvents(new RoomCreatedEvent("roomId", "test-room"));
    }

    @Test
    void testJoinChatRoom() {
        testFixture.given(new RoomCreatedEvent("roomId", "test-room"))
                   .when(new JoinRoomCommand("participant", "roomId"))
                   .expectEvents(new ParticipantJoinedRoomEvent("participant", "roomId"));
    }

    @Test
    void testPostMessage() {
        testFixture.given(new RoomCreatedEvent("roomId", "test-room"),
                          new ParticipantJoinedRoomEvent("participant", "roomId"))
                   .when(new PostMessageCommand("participant", "roomId", "Hi there!"))
                   .expectEvents(new MessagePostedEvent("participant", "roomId", "Hi there!"));
    }

    @Test
    void testCannotJoinChatRoomTwice() {
        testFixture.given(new RoomCreatedEvent("roomId", "test-room"),
                          new ParticipantJoinedRoomEvent("participant", "roomId"))
                   .when(new JoinRoomCommand("participant", "roomId"))
                   .expectSuccessfulHandlerExecution()
                   .expectNoEvents();
    }

    @Test
    void testCannotLeaveChatRoomTwice() {
        testFixture.given(new RoomCreatedEvent("roomId", "test-room"),
                          new ParticipantJoinedRoomEvent("participant", "roomId"),
                          new ParticipantLeftRoomEvent("participant", "roomId"))
                   .when(new LeaveRoomCommand("participant", "roomId"))
                   .expectSuccessfulHandlerExecution()
                   .expectNoEvents();
    }

    @Test
    void testParticipantCannotPostMessagesOnceHeLeftTheRoom() {
        testFixture.given(new RoomCreatedEvent("roomId", "test-room"),
                          new ParticipantJoinedRoomEvent("participant", "roomId"),
                          new ParticipantLeftRoomEvent("participant", "roomId"))
                   .when(new PostMessageCommand("participant", "roomId", "Hi there!"))
                   .expectException(IllegalStateException.class)
                   .expectNoEvents();
    }
}
