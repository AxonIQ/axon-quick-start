package io.axoniq.labs.chat.query.rooms.summary;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class RoomSummary {

    @Id
    private String roomId;
    private String name;
    private int participants;

    public RoomSummary() {
    }

    public RoomSummary(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public void addParticipant() {
        this.participants++;
    }

    public void removeParticipant() {
        this.participants--;
    }

    public int getParticipants() {
        return participants;
    }
}
