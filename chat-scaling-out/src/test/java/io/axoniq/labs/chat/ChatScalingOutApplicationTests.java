package io.axoniq.labs.chat;

import org.h2.tools.Server;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ChatScalingOutApplicationTests {

    private static Server server;

    @BeforeAll
    static void beforeClass() throws Exception {
        server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
        server.start();
    }

    @AfterAll
    static void afterClass() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void contextLoads() {
    }
}
