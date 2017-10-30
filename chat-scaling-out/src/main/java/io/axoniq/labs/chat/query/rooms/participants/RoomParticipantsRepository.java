package io.axoniq.labs.chat.query.rooms.participants;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomParticipantsRepository extends JpaRepository<RoomParticipant, Long> {

    List<RoomParticipant> findRoomParticipantsByRoomId(String roomId);

    void deleteByParticipantAndRoomId(String participant, String roomId);
}
