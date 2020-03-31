package io.axoniq.labs.chat.coreapi

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreateRoomCommand(@TargetAggregateIdentifier val roomId: String, val name: String)
data class JoinRoomCommand(@TargetAggregateIdentifier val roomId: String, val participant: String)
data class PostMessageCommand(@TargetAggregateIdentifier val roomId: String, val participant: String, val message: String)
data class LeaveRoomCommand(@TargetAggregateIdentifier val roomId: String, val participant: String)

data class RoomCreatedEvent(val roomId: String, val name: String)
data class ParticipantJoinedRoomEvent(val roomId: String, val participant: String)
data class MessagePostedEvent(val roomId: String, val participant: String, val message: String)
data class ParticipantLeftRoomEvent(val roomId: String, val participant: String)

class AllRoomsQuery
data class RoomParticipantsQuery(val roomId: String)
data class RoomMessagesQuery(val roomId: String)