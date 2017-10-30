package io.axoniq.labs.chat.commandmodel;

import io.axoniq.labs.chat.coreapi.*;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.Before;
import org.junit.Test;

public class ChatRoomTest {
    private AggregateTestFixture<ChatRoom> testFixture;

    @Before
    public void setUp() throws Exception {
        testFixture = new AggregateTestFixture<>(ChatRoom.class);
    }

    @Test
    public void testCreateChatRoom() throws Exception {
        testFixture.givenNoPriorActivity()
                   .when(new CreateRoomCommand("roomId", "testroom"))
                   .expectEvents(new RoomCreatedEvent("roomId", "testroom"));
    }

    @Test
    public void testJoinChatRoom() throws Exception {
        testFixture.given(new RoomCreatedEvent("roomId", "testroom"))
                   .when(new JoinRoomCommand("participant", "roomId"))
                   .expectEvents(new ParticipantJoinedRoomEvent("participant", "roomId"));
    }
    @Test
    public void testPostMessage() throws Exception {
        testFixture.given(new RoomCreatedEvent("roomId", "testroom"),
                          new ParticipantJoinedRoomEvent("participant", "roomId"))
                   .when(new PostMessageCommand("participant", "roomId", "Hi there!"))
                   .expectEvents(new MessagePostedEvent("participant", "roomId", "Hi there!"));
    }

    @Test
    public void testCannotJoinChatRoomTwice() throws Exception {
        testFixture.given(new RoomCreatedEvent("roomId", "testroom"),
                          new ParticipantJoinedRoomEvent("participant", "roomId"))
                   .when(new JoinRoomCommand("participant", "roomId"))
                   .expectSuccessfulHandlerExecution()
                   .expectNoEvents();
    }

    @Test
    public void testCannotLeaveChatRoomTwice() throws Exception {
        testFixture.given(new RoomCreatedEvent("roomId", "testroom"),
                          new ParticipantJoinedRoomEvent("participant", "roomId"),
                          new ParticipantLeftRoomEvent("participant", "roomId"))
                   .when(new LeaveRoomCommand("participant", "roomId"))
                   .expectSuccessfulHandlerExecution()
                   .expectNoEvents();
    }

    @Test
    public void testParticipantCannotPostMessagesOnceHeLeftTheRoom() throws Exception {
        testFixture.given(new RoomCreatedEvent("roomId", "testroom"),
                          new ParticipantJoinedRoomEvent("participant", "roomId"),
                          new ParticipantLeftRoomEvent("participant", "roomId"))
                   .when(new PostMessageCommand("participant", "roomId", "Hi there!"))
                   .expectException(IllegalStateException.class)
                   .expectNoEvents();
    }
}
