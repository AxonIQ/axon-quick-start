package io.axoniq.labs.chat.query.rooms.messages;

import io.axoniq.labs.chat.coreapi.MessagePostedEvent;
import io.axoniq.labs.chat.coreapi.RoomMessagesQuery;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ChatMessageProjection {

    private final ChatMessageRepository repository;
    private final QueryUpdateEmitter updateEmitter;

    public ChatMessageProjection(ChatMessageRepository repository, QueryUpdateEmitter updateEmitter) {
        this.repository = repository;
        this.updateEmitter = updateEmitter;
    }

    @QueryHandler
    public List<ChatMessage> handle(RoomMessagesQuery query) {
        return repository.findAllByRoomIdOrderByTimestamp(query.getRoomId());
    }

    @EventHandler
    public void on(MessagePostedEvent event, @Timestamp Instant timestamp) {
        ChatMessage chatMessage = new ChatMessage(event.getParticipant(),
                                                  event.getRoomId(),
                                                  event.getMessage(),
                                                  timestamp.toEpochMilli());
        repository.save(chatMessage);
        updateEmitter.emit(RoomMessagesQuery.class, query -> query.getRoomId().equals(event.getRoomId()), chatMessage);
    }
}
