Axon Lab - Getting Started
==========================

So, you're new to Axon and want to get started. Awesome!

Application overview
--------------------

The main application is called `ChatGettingStartedApplication`. It's a Spring Boot application with 
the following main dependencies:
 - Axon (Spring Boot starter)
 - Spring Data JPA
 - Freemarker
 - Web 
 - Reactor
 - H2 (Embedded Database)
 - Spring Boot Test
 - Axon Test

There are a few test cases. One will check if the application can start, while the others 
validate the Aggregate's behavior. They should all fail, as most of the stuff needs yet to be implemented.

### Application layout ###

The application's logic is divided among a number of packages.

- `io.axoniq.labs.chat`  
  The main package. Contains the Application class with the configuration.
- `io.axoniq.labs.chat.commandmodel`  
  Contains the Command Model. In our case, just the `ChatRoom` Aggregate that has been provided to make the project 
  compile.
- `io.axoniq.labs.chat.coreapi`  
  The so called *core api*. This is where we put the Commands, Events and Queries. 
  Since commands, events and queries are immutable, we have used Kotlin to define them. Kotlin allows you to
  concisely define each event, command and query on a single line.  
  To make sure you don't waste your precious time, we've implemented these Commands, Events and Queries for you.
- `io.axoniq.labs.chat.query.rooms.messages`  
  Contains the Projections (also called View Model or Query Model) for the Messages that have been broadcast in a 
  specific room. This package contains both the Event Handlers for updating the Projections, 
  as well as the Query Handlers to read the data.
- `io.axoniq.labs.chat.query.rooms.participants`  
  Contains the Projection to serve the list of participants in a given Chat Room. 
- `io.axoniq.labs.chat.query.rooms.summary`  
  Contains the Projection to serve a list of available chat rooms and the number of participants.
- `io.axoniq.labs.chat.restapi`  
  This is the REST Command and Query API to change and read the application's state. 
  API calls here are translated into Commands and Queries for the application to process.

### Swagger UI ###
The application has 'Swagger' enabled. You can use Swagger to send requests.

Visit: [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

<b>Note</b>: The Swagger UI does not support the 'Subscription Query' further on in the assignment,
 as Swagger does not support streaming results. 
Issuing a regular `curl` operation, or something along those lines, is recommended to check the Subscription Query.

<b>Note 2</b>: If you are on Intellij IDEA, you can also use the `command-request.http`
 and `query-request.http` files in this project to send requests directly from your IDE.
Several defaults have been provided, but feel free to play around here!

### H2 Console ###
The application has the 'H2 Console' configured, so you can peek into the database's contents.

Visit: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
Enter JDBC URL: `jdbc:h2:mem:testdb`  
Leave other values to defaults and click 'connect'.

Preparation
-----------

Axon Framework works best with AxonServer, and in this sample project we assume that you are using it. 
AxonServer needs to be downloaded separately.
You can run AxonServer as a docker container by running:
```shell script
docker run -d -p 8024:8024 -p 8124:8124 -p 8224:8224 --name axonserver axoniq/axonserver
```

Our goal
--------

Basically, the goal is simple: make the tests pass!

But testing isn't the ultimate goal: we want to have an application that we can take into production.

Exercises
---------

### Implement the Command Model ###

First of all, we're going to implement the Command Model. In this application, there is a single Aggregate: `ChatRoom`.
This aggregate processes Commands and produces Events as a result.

The expected behavior has been described in the `ChatRoomTest` class, using the Axon Test Fixtures.

To make these tests pass, you will need to implement the following command handlers:
1. The handler for the `CreateRoomCommand` creates a new instance of a `ChatRoom`. 
   Therefore, this command handler is a constructor, instead of a regular method. 
   The method should `apply` (static method on `AggregateLifecycle`) a `RoomCreatedEvent`. 
   
   Axon requires a no-arg constructor, we will also need to create one.

   Axon requires one field to be present: the aggregate's identifier. 
   Create a field called roomId of type String, and annotate it with `@AggregateIdentifier`.
    
   We will also need to set this field to the correct value. 
   As we are using event sourcing, we must do so in an `@EventSourcingHandler`. 
   Create one that reacts to the `RoomCreatedEvent` and sets the `roomId` to the correct value.  
2. The handler for the `JoinRoomCommand` should apply a `ParticipantJoinedRoomEvent`,
    but only if the joining participant hasn't already joined this room. 
   Otherwise, nothing happens. To do this, we will need to maintain some state. 
   We do this in `@EventSourcingHandler`, remember? Create the required handlers.
3. The handler for the `LeaveRoomCommand` should apply a `ParticipantLeftRoomEvent`,
    but only if the leaving participant has joined the room. 
   Otherwise, again, nothing happens. 
   Don't forget to update state in the right location.
4. Finally, implement the handler for the `PostMessageCommand`. 
   A participant may only post messages to rooms he/she has joined. 
   Otherwise, an exception is thrown.
   
    Now, there is only one thing left to do:

5. We need to tell Axon that we want to configure this class as an Aggregate. 
   Annotate it with `@Aggregate` to have the Axon Spring Boot Auto-Configuration module configure
    the necessary components to work with this aggregate.
   
That's it. All tests should pass now. If not, implement the missing behavior and try again...
 
### Connect the REST API to the Command Bus ###

We've got a component that can handle commands now. Now, it's time to allow external components to trigger these 
commands. The `CommandController` class defines some API endpoints that should trigger commands to be sent.

In Axon, we can use either the Command Bus or the CommandGateway to send commands. 
The latter has a friendlier API, so we've decided to use that one.

 1. Implement the TODOs in the `CommandController` class to forward Commands to the Command Bus. 
    Note that the API Endpoint methods declare a return type of `Future<...>`. 
    The `CommandGateway.send()` method also returns a Future. 
    This is a nice way to prepare the API layer for asynchronous execution of commands (perhaps later).

 2. The `CreateChatRoom` API declares an ID as part of the HTTP Entity it expects. 
    Although we generally favor client-generated identifiers, Javascript is notoriously bad at generating random values. 
    Therefore, we would want the `roomId` to default to a proper randomly generated UUID (use `UUID.randomUUID().toString()`). 
   
    Note that this API Endpoint returns a `Future<String>` (as opposed to `Future<Void>`). 
    Axon returns the identifier of an Aggregate as a result of a Command creating a new Aggregate instance. 
   
That's it! Once you're done, you should be able to start the application and send messages.
Remember that [Swagger](http://localhost:8080/swagger-ui/) is in place to help with this.
Additionally, if you are on Intellij IDEA you can use the `command-request.http` file to execute some REST operations too.

Note that the queries are not implemented yet. That's fixed in the next step.

### Implement the Projections ###

Now that the application is able to change state, it would be nice to expose that state. 
This is done in projections.

We need to implement three projections for this application:
  
  1. The `ChatMessageProjection` exposes the list of messages for a given chat room. 
     Implement an `@EventHandler` for the `MessagePostedEvent`. 
     
     Note that the `ChatMessage` Entity expects a timestamp. Axon attaches information to Events, which you can
     access in the Event Handlers. Add an extra parameter: `@Timestamp Instant timestamp`. Axon will automatically 
     inject the timestamp at which the message was originally created. Use `timestamp.toEpochMilli()` to convert it to
     milliseconds-since-epoch.
     
  2. Implement the `@EventHandler`s for the `RoomParticipantProjection`. This projection keeps track of all the 
     participants in each chatroom. You will need to implement an `@EventHandler` for each of the Events that describe 
     a change in the participants of a room...
     
  3. The last projection, the `RoomSummaryProjection`, gives us a summary of all the available chat rooms. The summary 
     contains the name of the room, and the number of participants in it. It's up to you to implement it.
     
  4. Implement the `@QueryHandler`s needed to extract data from all the projections. You will need to implement a    
     `@QueryHandler` for each Query defined in *core api*. So, implement an `AllRoomsQuery` handler in 
     `RoomSummaryProjection`, a `RoomParticipantsQuery` handler in `RoomParticipantProjection`, and a 
     `RoomMessagesQuery` handler in `ChatMessageProjection`.
 
  5. The projection `ChatMessageProjection` gives us a list of all the messages in a chat rooms. In order to support
     a 'Subscription Query' we will need to use the `QueryUpdateEmitter`  in the `@EventHandler` to send an update for 
     each new chat message.

### Connect the REST API to the Query Bus ###

We've got a component that can handle queries now. Now, it's time to allow external components to trigger these 
queries. The `QueryController` class defines some API endpoints that should trigger queries to be sent.

We can use either the `QueryBus` or the `QueryGateway` to send queries. The latter has a friendlier API, so 
we've decided to use that one.

 1. Implement the TODOs in the `QueryController` class to forward Queries to the `QueryBus`. Note that the API 
    Endpoint methods declare a return type of `Future<...>`. The `QueryGateway.query()` method also returns a `Future`. 
 2. Note that `subscribeRoomMessages()` endpoint declares a return type of `Flux<...>`. In this service you can send a 
    Subscription Query, by using the `QueryGateway#subscriptionQuery(...)` method, instead of a regular query in order 
    to be notified about new messages sent into the room. 

When you think you're done, give the application a spin and see what happens...
Remember, you can use [Swagger](http://localhost:8080/swagger-ui/)
 or the `query-request.http` (if you use Intellij IDEA) to test the new endpoints. 

# Done! Hurrah! #
