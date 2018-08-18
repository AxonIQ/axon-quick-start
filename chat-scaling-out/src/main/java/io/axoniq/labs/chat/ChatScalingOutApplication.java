package io.axoniq.labs.chat;

import com.rabbitmq.client.Channel;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.sql.SQLException;

@SpringBootApplication
public class ChatScalingOutApplication {

	private static final Logger logger = LoggerFactory.getLogger(ChatScalingOutApplication.class);

	public static void main(String[] args) throws SQLException {
		SpringApplication.run(ChatScalingOutApplication.class, args);
	}

	@Configuration
    @EnableSwagger2
    public static class SwaggerConfig {
        @Bean
        public Docket api() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .select()
                    .apis(RequestHandlerSelectors.any())
                    .paths(PathSelectors.any())
                    .build();
        }
    }

    @Configuration
    public static class SpringAmqp {

        @Bean
        @Autowired
        public Exchange eventsExchange(AmqpAdmin amqpAdmin) {
            Exchange eventsExchange = ExchangeBuilder.fanoutExchange("events").build();
            amqpAdmin.declareExchange(eventsExchange);

            return eventsExchange;
        }

        @Bean
        @Autowired
        public Queue participantEventsQueue(AmqpAdmin amqpAdmin) {
            Queue participantEventsQueue = QueueBuilder.durable("participant-events").build();
            amqpAdmin.declareQueue(participantEventsQueue);

            return participantEventsQueue;
        }

        @Bean
        @Autowired
        public Binding participantEventsBinding(AmqpAdmin amqpAdmin, Exchange eventsExchange, Queue participantEventsQueue) {
            Binding eventsBinding = BindingBuilder
                    .bind(participantEventsQueue)
                    .to(eventsExchange)
                    .with("*")
                    .noargs();
            amqpAdmin.declareBinding(eventsBinding);

            return eventsBinding;
        }

        @Bean
        @Autowired
        public SpringAMQPMessageSource participantEvents(Serializer serializer) {
            return new SpringAMQPMessageSource(serializer) {
                @Override
                @RabbitListener(queues = "participant-events")
                public void onMessage(Message message, Channel channel) {
                    super.onMessage(message, channel);
                }
            };
        }
    }
}
