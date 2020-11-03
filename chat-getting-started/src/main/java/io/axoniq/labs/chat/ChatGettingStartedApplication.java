package io.axoniq.labs.chat;

import com.google.common.base.Predicates;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class ChatGettingStartedApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatGettingStartedApplication.class, args);
    }

    @Configuration
    @EnableSwagger2
    public static class SwaggerConfig {

        @Bean
        public Docket api() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("io.axoniq.labs.chat"))
                    .paths(PathSelectors.any())
                    .build();
        }
    }
}
