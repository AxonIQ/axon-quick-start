package io.axoniq.labs.chat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class ChatGettingStartedApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatGettingStartedApplication.class, args);
    }

    @Configuration
    public static class SwaggerConfig {

        @Bean
        public OpenAPI cloudOpenAPI() {
            return new OpenAPI()
                    .info(new Info().title("Chat Getting Started")
                                    .description(
                                            "Application for developers that would like to take the first steps with Axon Framework"));
        }
    }
}