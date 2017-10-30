package io.axoniq.labs.chat.coreapi

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CreateRoomCommand(@TargetAggregateIdentifier val roomId: String, val name: String)
data class JoinRoomCommand(val participant: String, @TargetAggregateIdentifier val roomId: String)
data class PostMessageCommand(val participant: String, @TargetAggregateIdentifier val roomId: String, val message: String)
data class LeaveRoomCommand(val participant: String, @TargetAggregateIdentifier val roomId: String)

data class RoomCreatedEvent(val roomId: String, val name: String)
data class ParticipantJoinedRoomEvent(val participant: String, val roomId: String)
data class MessagePostedEvent(val participant: String, val roomId: String, val message: String)
data class ParticipantLeftRoomEvent(val participant: String, val roomId: String)
