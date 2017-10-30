package io.axoniq.labs.chat.query.rooms.messages;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

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

    // TODO: Create some event handlers that update this model when necessary

}
