package com.example.mq.study;

import com.example.mq.mqserver.VirtualHost;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Study {
    
    private ServerSocket serverSocket = null;
    
    private VirtualHost virtualHost = new VirtualHost("default");
    private ConcurrentHashMap<String, Socket> sessions = new ConcurrentHashMap<>();
    private ExecutorService executorService = null;
    private volatile boolean runnable = true;
    
    public Study(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    
    public void start() {
        System.out.println("[BrokerServer] 启动！");
        executorService = Executors.newCachedThreadPool();
        try {
            while (runnable) {
                Socket clientSocket = serverSocket.accept();
                
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}