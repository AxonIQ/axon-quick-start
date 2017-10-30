package io.axoniq.labs.chat.query.rooms.messages;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findAllByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);

}
