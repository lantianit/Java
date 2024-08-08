package com.atguigu.rabbitmq.simple;

import com.rabbitmq.client.ConnectionFactory;

public class Study {

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        
        connectionFactory.setHost("");
        connectionFactory.setPort(2);
        connectionFactory.setVirtualHost(".");
        
        
    }
    
}