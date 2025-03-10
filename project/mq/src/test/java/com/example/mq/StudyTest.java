package com.example.mq;

import com.example.mq.mqserver.core.Binding;
import com.example.mq.mqserver.core.Exchange;
import com.example.mq.mqserver.core.MSGQueue;
import com.example.mq.mqserver.core.Message;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class StudyTest {
    
    private ConcurrentHashMap<String, Exchange> exchangeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, MSGQueue> queueMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Binding>> bindingsMap = new ConcurrentHashMap<>();
    
    private ConcurrentHashMap<String, Message> messageMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, LinkedList<Message>> queueMessageMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Message>> queueMessageWaitAckMap = new ConcurrentHashMap<>();
    
}