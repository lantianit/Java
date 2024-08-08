package com.example;

import com.example.mq.MqApplication;
import com.example.mq.mqserver.VirtualHost;
import com.example.mq.mqserver.core.ExchangeType;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

@SpringBootTest
public class t {
    
    private VirtualHost virtualHost = null;
    
    @BeforeEach
    public void setUp() {
        MqApplication.context = SpringApplication.run(MqApplication.class);
        virtualHost = new VirtualHost("default");
    }
    
    @AfterEach
    public void tearDown() throws IOException {
        MqApplication.context.close();
        virtualHost = null;
        File dataDir = new File("./data");
        FileUtils.deleteDirectory(dataDir);
    }
    
    @Test
    public void testExchangeDeclare() {
        boolean ok = virtualHost.exchangeDeclare("testExchange", ExchangeType.DIRECT, true, false, null);
        Assertions.assertTrue(ok);
    }
    
}