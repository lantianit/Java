package com.example.mq.study;

import com.example.mq.common.ConsumerEnv;
import com.example.mq.common.MqException;
import com.example.mq.mqserver.VirtualHost;
import com.example.mq.mqserver.core.*;
import com.example.mq.mqserver.datacenter.DiskDataCenter;
import com.example.mq.mqserver.datacenter.MemoryDataCenter;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Study {
    
    private String virtualHostName;
    
    private MemoryDataCenter memoryDataCenter = new MemoryDataCenter();
    
    private DiskDataCenter diskDataCenter = new DiskDataCenter();
    private Router router = new Router();
    private ConsumerManager consumerManager = new ConsumerManager();
    
    private final Object exchangeLocker = new Object();
    private final Object queueLocker = new Object();
    
    public String getVirtualHostName() {
        return virtualHostName;
    }
    
    public MemoryDataCenter getMemoryDataCenter() {
        return memoryDataCenter;
    }
    
    public DiskDataCenter getDiskDataCenter() {
        return diskDataCenter;
    }
    
    public Study(String queueName) {
        this.virtualHostName = queueName;
        diskDataCenter.init();
        try {
            memoryDataCenter.recovery(diskDataCenter);
        } catch (IOException | MqException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
}

class ConsumerManager {
    
    private VirtualHost parent;
    private ExecutorService workerPool = Executors.newFixedThreadPool(4);
    private BlockingQueue<String> tokenQueue = new LinkedBlockingQueue<>();
    private Thread scannerThread = null;
    
    public c(VirtualHost p) {
        parent = p;
        scannerThread = new Thread(() -> {
           while (true) {
               try {
                   String queueName = tokenQueue.take();
                   MSGQueue queue = parent.getMemoryDataCenter().getQueue(queueName);
                   synchronized (queue) {
                       consumeMessage(queue);
                   }
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }
        });
        
    }

    private void consumeMessage(MSGQueue queue) {

        ConsumerEnv luckDog = queue.chooseConsumer();
        if (luckDog == null) {
            return;
        }
        Message message = parent.getMemoryDataCenter().pollMessage(queue.getName());
        if (message == null) {
            return;
        }
        workerPool.submit(() -> {
            try {
                parent.getMemoryDataCenter().addMessageWaitAck(queue.getName(), message);
                luckDog.getConsumer().handleDelivery(luckDog.getConsumerTag(), message.getBasicProperties(), message.getBody());
                
            }
        })
    }

}

class ConsumerManager2 {
    private VirtualHost parent;
    private ExecutorService wokrerPool = Executors.newFixedThreadPool(4);
    private BlockingQueue<String> tokenQueue = new LinkedBlockingQueue<>();
    private Thread scannerThread = null;
    
    public ConsumerManager2(VirtualHost p) {
        parent = p;
        
        scannerThread = new Thread(() -> {
           while (true) {
               try {
                   String queueName = tokenQueue.take();
                   MSGQueue queue = parent.getMemoryDataCenter().getQueue(queueName);
                   if (queue == null) {
                       throw new MqException("");
                   }
                   synchronized (queue) {
                       consumeMessage(queue);
                   }
               } catch (InterruptedException | MqException e) {
                   throw new RuntimeException(e);
               }
           }
        });
        
    }

    private void consumeMessage(MSGQueue queue) {
        ConsumerEnv luckyDog = queue.chooseConsumer();
        if (luckyDog == null) {
            return;
        }
        Message message = parent.getMemoryDataCenter().pollMessage(queue.getName());
        if (message == null) {
            return;
        }
        
    }


}
