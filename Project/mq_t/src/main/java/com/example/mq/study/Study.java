package com.example.mq.study;

import com.example.mq.common.MqException;
import com.example.mq.mqserver.core.*;
import com.example.mq.mqserver.datacenter.DiskDataCenter;
import com.example.mq.mqserver.datacenter.MemoryDataCenter;

import java.util.Map;

public class Study {
    
    private String virtualHostName;
    private MemoryDataCenter memoryDataCenter = new MemoryDataCenter();
    private DiskDataCenter diskDataCenter = new DiskDataCenter();
    
    private Router router = new Router();
    
    private ConsumerManager consumerManager = new ConsumerManager();
    private final Object exchangeLocker = new Object();

    public String getVirtualHostName() {
        return virtualHostName;
    }

    public MemoryDataCenter getMemoryDataCenter() {
        return memoryDataCenter;
    }

    public DiskDataCenter getDiskDataCenter() {
        return diskDataCenter;
    }
    
    public boolean exchangeDelete(String exchangeName) {
        exchangeName = virtualHostName + exchangeName;
        try {
            synchronized (exchangeLocker) {
                Exchange toDelete = memoryDataCenter.getExchange(exchangeName);
                if (toDelete == null) {
                    throw new MqException("")
                }
            }
        }
    }
    
    public boolean basicPublish(String exchangeName, String routingKey, BasicProperties basicProperties, byte[] body) {
        try {
            exchangeName = virtualHostName + exchangeName;
            if (!router.checkRoutingKey(routingKey)) {
                throw new MqException("[VirtualHost] routingKey 非法! routingKey=" + routingKey);
            }
            Exchange exchange = memoryDataCenter.getExchange(exchangeName);
            if (exchange == null) {
                throw new MqException("")
            }
        } catch (MqException e) {
            throw new RuntimeException(e);
        }

    }
    
}

















