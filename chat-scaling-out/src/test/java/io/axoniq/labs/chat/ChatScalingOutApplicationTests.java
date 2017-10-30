package io.axoniq.labs.chat;

import org.h2.tools.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChatScalingOutApplicationTests {

    private static Server server;

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
        server.start();
    }

    @AfterClass
    public static void afterClass() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void contextLoads() {
    }

}
