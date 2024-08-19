package com.example.mq.study;


import com.example.mq.mqserver.core.BasicProperties;
import com.example.mq.mqserver.core.Message;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;

import java.util.UUID;

public class Study {
    private BasicProperties basicProperties = new BasicProperties();
    private byte[] body;
    private transient long offsetBeg = 0;
    private transient long offsetEnd = 0;
    private byte isValisd = 0x1;
    
    public static Message createMessageWithId(String routingKey, BasicProperties basicProperties, byte[] body) {
        Message message = new Message();
        if (basicProperties != null) {
            message.setBasicProperties(basicProperties);
        }
        message.setMessageId("M-" + UUID.randomUUID());
        message.setRoutingKey(routingKey);
        message.body = 
    }
    
}
