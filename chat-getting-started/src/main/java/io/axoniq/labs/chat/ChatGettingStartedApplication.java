package io.axoniq.labs.chat;

import com.google.common.base.Predicates;
import org.axonframework.commandhandling.AsynchronousCommandBus;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.interceptors.TransactionManagingInterceptor;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PreDestroy;

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
                    .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework")))
                    .paths(PathSelectors.any())
                    .build();
        }
    }

    @Configuration
    public static class AsynchronousCommandBusConfig {

        private TransactionManager transactionManager;

        @Autowired
        public AsynchronousCommandBusConfig(TransactionManager transactionManager) {
            this.transactionManager = transactionManager;
        }

        @Bean
        public CommandBus commandBus() {
            CommandBus commandBus = new AsynchronousCommandBus();
            commandBus.registerHandlerInterceptor(new TransactionManagingInterceptor<>(transactionManager));

            return commandBus;
        }

        @PreDestroy
        public void stop() {
            ((AsynchronousCommandBus) commandBus()).shutdown();
        }
    }
}
