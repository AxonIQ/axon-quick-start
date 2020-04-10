package io.axoniq.labs.chat.query.rooms.participants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"roomId", "participant"}))
public class RoomParticipant {

    @Id
    @GeneratedValue
    private Long id;

    private String roomId;
    private String participant;

    public RoomParticipant() {
    }

    public RoomParticipant(String roomId, String participant) {
        this.roomId = roomId;
        this.participant = participant;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getParticipant() {
        return participant;
    }
}
