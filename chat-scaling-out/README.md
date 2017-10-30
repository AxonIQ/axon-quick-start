Axon Lab - Scaling out
======================

So, you have selected the advanced Lab. Good job!

Application overview
--------------------

The main application is called `ChatScalingOutApplication`. It's a Spring Boot application with 
the following main dependencies:
 - Axon (spring boot starter)
 - Spring Data JPA
 - Freemarker
 - Web 
 - Spring Boot Test
 - Axon Test

Because we will be having multiple instances cooperating on the same database, we can't use an
embedded H2 database anymore. You can run the `Servers` class to start an H2 database with a
TCP endpoint. The application is configured to connect to this database. 

There are a few test cases. One will check if the application can start, while the others 
validate the Aggregate's behavior. They should all pass.

### Application layout ###

The application's logic is divided among a number of packages.

- `io.axoniq.labs.chat`  
  the main package. Contains the Application class with the configuration.
- `io.axoniq.labs.chat.commandmodel`  
  contains the Command Model. In our case, just the `Room` Aggregate.
- `io.axoniq.labs.chat.coreapi`  
  The so called *core api*. This is where we put the Commands and Events. 
  Since both commands and events are immutable, we have used Kotlin to define them. Kotlin allows use to
  concisely define each event and command on a single line.
- `io.axoniq.labs.chat.query.rooms.messages`  
  Contains the Projections (also called View Model or Query Model) for the Messages that have been broadcast in a 
  specific room. This package contains both the Event Handlers for updating the Projections, as well as the Rest 
  Controllers to expose the data through a Rest API.
- `io.axoniq.labs.chat.query.rooms.participants`  
  Contains the Projection to serve the list of participants in a given Chat Room. 
- `io.axoniq.labs.chat.query.rooms.summary`  
  Contains the Projection to serve a list of available chat rooms and the number of participants
- `io.axoniq.labs.chat.roomapi`  
  This is the Command API to change the application's state. API calls here are translated into Commands for the
  application to process

### Swagger UI ###
The application has Swagger enabled. You can use Swagger to send requests.

Visit: http://localhost:8080/swagger-ui.html

### H2 Console ###
The application has the H2 Console configured, so you can peek into the database's contents.

Visit: http://localhost:8080/h2-console  
Enter JDBC URL: jdbc:h2:tcp://localhost:9092/mem:testdb  
Leave other values to defaults and click 'connect'

Our goal
--------

Obviously, this Chat application is expected to be a massive success, and it needs to be ready to scale to massive 
proportions. Therefore, we are going to configure the application to work with multiple nodes efficiently.

Commands will have to be consistently routed based on the Chat Room ID that they target. Event processing will have to
be distributed as well. We are going to use a combination of Tracking Processors and Rabbit MQ to do this.

Preparation
-----------

If you want to use RabbitMQ, you will need to install it. The easiest way to start it, is by running it in a docker 
container. An image with a management interface is available under the tag `management` or `management-alpine`. This 
interface may come in handy.

```bash
docker create --name rabbitmq -p 4369:4369 -p 5671:5671 -p 5672:5672 -p 15671:15671 -p 15672:15672 -p 25672:25672 rabbitmq:management-alpine
docker start rabbitmq
```

If you don't have Docker, you can [download](http://www.rabbitmq.com/download.html) and install RabbitMQ the regular 
way.

Exercises
---------

### Distributed Command Bus ###

First of all, we're going to make sure that Command processing is properly load balanced between all available nodes.
For this, Axon has a `DistributedCommandBus`. It uses a `CommandBusConnector` and `CommandRouter` to transport Commands
between nodes. Axon currently supports two connectors: JGroups and Spring Cloud. For this lab, we will be using J
Groups.

As we are using Spring Boot, we can use the auto-configuration mechanism to enable the Distributed Command Bus. To do
so, do the following:

 1. Add a dependency to `axon-spring-boot-starter-jgroups` in pom.xml (you can change the existing `axon-spring-boot-starter` 
    dependency). This will add JGroups and Axon's Distributed Command Bus modules to the class path.

 2. Having JGroups and the Distributed Command Bus on the class path isn't enough to kick it off. We need to enable it 
    explicitly in `application.properties`.
    
    a. Add a property `axon.distributed.enabled=true` to enable the Distributed Command Bus
    b. Set the bind address to the loopback address, to prevent collisions with you neighbour:  
       `axon.distributed.jgroups.bind-addr=127.0.0.1` 

You're done. By explicitly enabling the distributed command bus and having JGroups on the classpath,
Axon will configure a Distributed Command Bus bean with the proper wiring for you. This implementation
uses Gossip to detect nodes. The Distributed Command Bus will look for the server at `localhost:12002` by default.

Now, start the database and Gossip Servers (using the `Servers` class' main method) and an instance of your application.
You'll notice JGroups is connecting in the output.

Start another application instance. This time, provide the startup parameter `-Dserver.port=8081` to prevent port 
conflicts. Send commands to one (or both) of the nodes through the Swagger UI or whichever Rest client you wish to use.

### Tracking Processor ###

By default, events are handled on the node (and thread) that publishes them. While this happens to work fine for our 
use case, this is not how we want it. Instead, we want to process events in a separate thread.

In Axon, you can use a so-called Tracking Processor to process events asynchronously. The processor will
tail the Events in the Event Store (or any other source that supports Tracking). It keeps track of which events it has 
processed through a TokenStore. Axon will automatically create a JPA based token store if it detects JPA on the 
classpath (which the application has).

To configure a processor to switch to Tracking mode:

1. We first want to override the Processing Group's name. By default, this name of a processing group (and the processor 
that will process events on behalf of it) is the package name of the event handlers that are assigned to it.  
The easiest way to override is to put a `@ProcessingGroup` annotation on the `ChatMessageProjection` class. Give it the 
value `messages`.
2. In `application.properties`, configure the `messages` processor to be tracking:  
`axon.eventhandling.processors.messages.mode=tracking`. (Note that the `messages` part is the name of the processor) 

Restart your application, and processing is now asynchronous. Check out the "TOKEN_ENTRY" table in the H2 Console to
see the token being updated.

Note:
If you start your application, and haven't reset your database, it is possible that messages will appear twice. That is,
because the processor will start from the beginning of the Event Store and replay all past events. Effectively, it will 
just re-insert all messages. So for now, just restart the `Servers` process to reset the database.

### Publishing Events to RabbitMQ ###

Sometimes, you want to publish Events from your application to a message broker. If that broker supports AMQP, that is 
easy to configure in Axon. In other cases, you want your processor to consume messages from an AMQP message broker, 
instead of the Event Bus or Event Store.

First, we are going to enable AMQP support:
1. Add a dependency to `org.springframework.boot:spring-boot-starter-amqp`. This will enable AMQP in Spring Boot.
2. Add a dependency to `org.axonframework:axon-amqp:3.0.6`, This will enable Axon AMQP support.

Now that AMQP is available, we need to define an Exchange, Queue and Binding for our application to use:
1. Define a Fanout Exchange called 'events'
2. Define a queue called 'participant-events'
3. Bind the queue to the exchange
4. Use AmqpAdmin to declare these components.

> Hints:  
> These components are defined as Spring Beans in the Application Configuration. Spring comes with classes to help 
you build these components:`ExchangeBuilder`, `QueueBuilder` and `BindingBuilder`.
>
> You can annotate methods with @Autowired to have Spring invoke them with parameters. Use this mechanism to inject
the `AmqpAdmin` bean and call the `declare...` methods on it.

Now that we have an Exchange, we can tell Axon to send all events there:
1. Add a property to `application.properties`: axon.amqp.exchange=events

That's it. Axon will automatically forward all events to the 'events' echange.

Start the application and open the [RabbitMQ Console](http://localhost:15672/) (username & password: guest)

### Consuming Events from Rabbit MQ ###

Now that we've got a queue with events, let's have a look at what it takes to read messages from it.

First we need to create a bean for the message source:
1. Define an `@Bean` of type `SpringAMQPMessageSource`. It needs a Serializer. You can autowire one by specifying it as
a parameter on your `@Bean` method.
2. We need to tell Spring to use this source when messages arrive on the `participant-events` queue:
   
   a. Instead of returning a regular instance of `SpringAMQPMessageSource`, create an anonymous subclass. (Just add `{}` before the `;`)
   b. Override the `onMessage` method, and have it call super (which is probably auto-generated by your IDE)
   c. Annotate the `onMessage` method with `@RabbitListener(queues = "participant-events")`

The `@RabbitListener` tells Spring that we want this method invoked when a message is available on the 
`participant-events` queue. Axon will then take it from there, and notify any Processors that are told to read from 
this source.

And that's what we need to do next:
1. Assign the `RoomParticipantsProjection` class to the `participant` processing group. (You've done this before, 
remember?)
2. In `application.properties`, define this bean as being the source:  
   `axon.eventhandling.processors.participants.source=participantEvents`

### Off the beaten track (Bonus Exercises) ###

At the moment, each application is exactly identical. You split the Command from the Query by using Spring Profiles.

Assign a Profile to the ChatRoom aggregate. By not enabling this profile, the instance will not register any command handlers.

Do the same (but with a different profile) for the Query components.

Since it's a bonus exercise, we're not giving too many hints. Play around a bit, and have fun!!

# Done! Hurrah! #
