package com.example.mq.study;

import com.example.mq.mqserver.VirtualHost;
import com.example.mq.mqserver.core.Exchange;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Study {
    
    private VirtualHost parent;
    private ExecutorService workerPool = Executors.newFixedThreadPool(4);
    private BlockingQueue<String> tokenQueue = new LinkedBlockingQueue<>();
    private Thread scannerThread = null;
    
    public Study(VirtualHost p) {
        parent = p;
        
        scannerThread = new Thread(() -> {
           while (true) { 
               try {
                   String queueName = tokenQueue.take();
               }
               }
        });
        
    }
    
}