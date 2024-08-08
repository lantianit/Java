package com.example.mq.study;

import com.example.mq.common.MqException;
import com.example.mq.mqserver.core.*;
import com.example.mq.mqserver.datacenter.DiskDataCenter;
import com.example.mq.mqserver.datacenter.MemoryDataCenter;

import java.io.IOException;
import java.util.Map;

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
    
    public Study(String name) throws RuntimeException {
        this.virtualHostName = name;
        diskDataCenter.init();
        try {
            memoryDataCenter.recovery(diskDataCenter);
        } catch (IOException | MqException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("");
        }
    }

    public boolean exchangeDeclare(String exchangeName, ExchangeType exchangeType, boolean durable, boolean autoDelete,
                                   Map<String, Object> arguments) {
        // 把交换机的名字, 加上虚拟主机作为前缀.
        exchangeName = virtualHostName + exchangeName;
        try {
            synchronized (exchangeLocker) {
                // 1. 判定该交换机是否已经存在. 直接通过内存查询.
                Exchange existsExchange = memoryDataCenter.getExchange(exchangeName);
                if (existsExchange != null) {
                    // 该交换机已经存在!
                    System.out.println("[VirtualHost] 交换机已经存在! exchangeName=" + exchangeName);
                    return true;
                }
                // 2. 真正创建交换机. 先构造 Exchange 对象
                Exchange exchange = new Exchange();
                exchange.setName(exchangeName);
                exchange.setType(exchangeType);
                exchange.setDurable(durable);
                exchange.setAutoDelete(autoDelete);
                exchange.setArguments(arguments);
                // 3. 把交换机对象写入硬盘
                if (durable) {
                    diskDataCenter.insertExchange(exchange);
                }
                // 4. 把交换机对象写入内存
                memoryDataCenter.insertExchange(exchange);
                System.out.println("[VirtualHost] 交换机创建完成! exchangeName=" + exchangeName);
                // 上述逻辑, 先写硬盘, 后写内存. 目的就是因为硬盘更容易写失败. 如果硬盘写失败了, 内存就不写了.
                // 要是先写内存, 内存写成功了, 硬盘写失败了, 还需要把内存的数据给再删掉. 就比较麻烦了.
            }
            return true;
        } catch (Exception e) {
            System.out.println("[VirtualHost] 交换机创建失败! exchangeName=" + exchangeName);
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean exchangeDelete(String exchangeName) {
        synchronized (exchangeLocker) {
            // 1. 先找到对应的交换机.
            Exchange toDelete = memoryDataCenter.getExchange(exchangeName);
            if (toDelete == null) {
                try {
                    throw new MqException("[VirtualHost] 交换机不存在无法删除!");
                } catch (MqException e) {
                    throw new RuntimeException(e);
                }
            }
            // 2. 删除硬盘上的数据
            if (toDelete.isDurable()) {
                diskDataCenter.deleteExchange(exchangeName);
            }
            // 3. 删除内存中的交换机数据
            memoryDataCenter.deleteExchange(exchangeName);
            System.out.println("[VirtualHost] 交换机删除成功! exchangeName=" + exchangeName);
        }
        return true;
    }
    
    public boolean basicAck(String queueName, String messageId) {
        queueName = virtualHostName + queueName;
    }
    
        
}