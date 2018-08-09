package io.axoniq.labs.chat.query.rooms.messages;

import io.axoniq.labs.chat.coreapi.MessagePostedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/rooms/{roomId}/messages")
public class ChatMessageProjection {

    private final ChatMessageRepository repository;

    public ChatMessageProjection(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<ChatMessage> messagesInRoom(@PathVariable String roomId,
                                            @RequestParam(defaultValue = "0") int pageId,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return repository.findAllByRoomIdOrderByTimestampDesc(roomId, new PageRequest(pageId, pageSize));
    }

    @EventHandler
    public void on(MessagePostedEvent messagePostedEvent, @Timestamp Instant timestamp) {
        repository.save(new ChatMessage(
                messagePostedEvent.getParticipant(),
                messagePostedEvent.getRoomId(),
                messagePostedEvent.getMessage(),
                timestamp.toEpochMilli()
        ));
    }
}
