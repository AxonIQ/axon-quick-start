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
 - Reactor
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
  contains the Command Model. In our case, just the `Room` Aggregate that has been provided to make the project 
  compile.
- `io.axoniq.labs.chat.coreapi`  
  The so called *core api*. This is where we put the Commands, Events and Queries. 
  Since commands, events and queries are immutable, we have used Kotlin to define them. Kotlin allows you to
  concisely define each event, command and query on a single line.  
  To make sure you don't waste your precious time, we've implemented these Commands, Events and Queries for you.
- `io.axoniq.labs.chat.query.rooms.messages`  
  Contains the Projections (also called View Model or Query Model) for the Messages that have been broadcast in a 
  specific room. This package contains both the Event Handlers for updating the Projections, 
  as well as the Query Handlers to read these data.
- `io.axoniq.labs.chat.query.rooms.participants`  
  Contains the Projection to serve the list of participants in a given Chat Room. 
- `io.axoniq.labs.chat.query.rooms.summary`  
  Contains the Projection to serve a list of available chat rooms and the number of participants
- `io.axoniq.labs.chat.restapi`  
  This is the REST Command and Query API to change and read the application's state. 
  API calls here are translated into Commands and Queries for the application to process.

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
be distributed as well. We are going to use AxonServer to do this.

Preparation
-----------

Axon Framework works best with Axon Server, and in this sample project we assume that you do. 
Axon Server needs to be downloaded separately.

Exercises
---------

### Fire up another instance ###

Connect a second instance of application to AxonServer. You just need to startup another instance; if you run them locally, 
remember to change the server port in `application.properties`, setting the `server.port=9090`.

Now you can invoke rest APIs to both instances indifferently. 
Try for example to subscribe for room messages to an instance and then to post messages from the other one.

### Parallel Processing of Events ###

To configure a tracking processor for parallel processing:

1. We first want to override the Processing Group's name. By default, this name of a processing group (and the processor 
that will process events on behalf of it) is the package name of the event handlers that are assigned to it.  
The easiest way to override is to put a `@ProcessingGroup` annotation on the `ChatMessageProjection` class. Give it the 
value `messages`.
2. In `application.properties`, configure the `messages` processor initial number of segments to define the maximum number of overall threads:  
`axon.eventhandling.processors.messages.initialSegmentCount=4`. (Note that the `messages` part is the name of the processor) 
3. In `application.properties`, set also maximum number threads to start on this node:  
`axon.eventhandling.processors.messages.threadCount=2`. 
4. In `application.properties`, set the processor mode to tracking:  
`axon.eventhandling.processors.messages.mode=tracking`. (Note that this is still required in Axon 4 milestone 2; 
however, the final release of Axon 4 will have tracking event processors by default, so you would not need this configuration)

Restart your applications, and processing is now parallel. Check out the "TOKEN_ENTRY" table in the H2 Console to
see the token being updated.

Note:
Remember to restart the `Servers` process to reset the database. 
The initialSegmentCount property is used only if the segments for that tracking processors are not yet defined in TOKEN_ENTRY table.

### Off the beaten track (Bonus Exercises) ###

At the moment, each application is exactly identical. You split the Command from the Query by using Spring Profiles.

Assign a Profile to the ChatRoom aggregate. By not enabling this profile, the instance will not register any command handlers.

Do the same (but with a different profile) for the Query components.

Since it's a bonus exercise, we're not giving too many hints. Play around a bit, and have fun!!

# Done! Hurrah! #
