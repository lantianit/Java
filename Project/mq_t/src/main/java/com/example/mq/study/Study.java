package com.example.mq.study;

import com.example.mq.mqserver.BrokerServer;
import com.example.mq.mqserver.VirtualHost;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

class Study {
    
    private ServerSocket serverSocket = null;
    private VirtualHost virtualHost = new VirtualHost("default");
    private ConcurrentHashMap<String, Socket> sessions = new ConcurrentHashMap<>();
    private ExecutorService executorService = null;
    private volatile boolean runnable = true;
    
    public 
    
}