package io.axoniq.labs.chat.restapi;

import io.axoniq.labs.chat.coreapi.AllRoomsQuery;
import io.axoniq.labs.chat.coreapi.RoomMessagesQuery;
import io.axoniq.labs.chat.coreapi.RoomParticipantsQuery;
import io.axoniq.labs.chat.query.rooms.messages.ChatMessage;
import io.axoniq.labs.chat.query.rooms.summary.RoomSummary;
import org.axonframework.messaging.responsetypes.MultipleInstancesResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.Future;

import static org.axonframework.messaging.responsetypes.ResponseTypes.instanceOf;
import static org.axonframework.messaging.responsetypes.ResponseTypes.multipleInstancesOf;


@RestController
public class QueryController {

    private final QueryGateway gateway;

    public QueryController(QueryGateway gateway) {
        this.gateway = gateway;
    }

    @GetMapping("rooms")
    public Future<List<RoomSummary>> listRooms() {
        return gateway.query(new AllRoomsQuery(), new MultipleInstancesResponseType<>(RoomSummary.class));
    }

    @GetMapping("/rooms/{roomId}/participants")
    public Future<List<String>> participantsInRoom(@PathVariable String roomId) {
        return gateway.query(new RoomParticipantsQuery(roomId), new MultipleInstancesResponseType<>(String.class));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public Future<List<ChatMessage>> roomMessages(@PathVariable String roomId) {
        return gateway.query(new RoomMessagesQuery(roomId), new MultipleInstancesResponseType<>(ChatMessage.class));
    }

    @GetMapping(value = "/rooms/{roomId}/messages/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatMessage> subscribeRoomMessages(@PathVariable String roomId) {
        RoomMessagesQuery query = new RoomMessagesQuery(roomId);
        SubscriptionQueryResult<List<ChatMessage>, ChatMessage> result;
        result = gateway.subscriptionQuery(query,
                                           multipleInstancesOf(ChatMessage.class),
                                           instanceOf(ChatMessage.class));
        /* If you only want to send new messages to the client, you could simply do:
                return result.updates();
           However, in our implementation we want to provide both existing messages and new ones,
           so we combine the initial result and the updates in a single flux. */
        Flux<ChatMessage> initialResult = result.initialResult().flatMapMany(Flux::fromIterable);
        return Flux.concat(initialResult, result.updates());
    }
}
