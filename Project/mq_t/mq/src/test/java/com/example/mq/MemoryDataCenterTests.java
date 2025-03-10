package com.example.mq;

import com.example.mq.common.MqException;
import com.example.mq.mqserver.core.*;
import com.example.mq.mqserver.datacenter.MemoryDataCenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MemoryDataCenterTests {

    private MemoryDataCenter memoryDataCenter = null;

    @BeforeEach
    public void setUp() {
        memoryDataCenter = new MemoryDataCenter();
    }

    @AfterEach
    public void tearDown() {
        memoryDataCenter = null;
    }

    private Exchange createTestExchange(String exchangeName) {
        Exchange exchange = new Exchange();
        exchange.setName(exchangeName);
        exchange.setType(ExchangeType.DIRECT);
        exchange.setAutoDelete(false);
        exchange.setDurable(true);
        return exchange;
    }

    private MSGQueue createTestQueue(String queueName) {
        MSGQueue queue = new MSGQueue();
        queue.setName(queueName);
        queue.setDurable(true);
        queue.setExclusive(false);
        queue.setAutoDelete(false);
        return queue;
    }

    @Test
    public void testExchange() {
        // 1. 先构造一个交换机并插入.
        Exchange expectedExchange = createTestExchange("testExchange");
        memoryDataCenter.insertExchange(expectedExchange);
        // 2. 查询出这个交换机, 比较结果是否一致. 此处直接比较这俩引用指向同一个对象.
        Exchange actualExchange = memoryDataCenter.getExchange("testExchange");
        Assertions.assertEquals(expectedExchange, actualExchange);
        // 3. 删除这个交换机
        memoryDataCenter.deleteExchange("testExchange");
        // 4. 再查一次, 看是否就查不到了
        actualExchange = memoryDataCenter.getExchange("testExchange");
        Assertions.assertNull(actualExchange);
    }

    // 针对队列进行测试
    @Test
    public void testQueue() {
        // 1. 构造一个队列, 并插入
        MSGQueue expectedQueue = createTestQueue("testQueue");
        memoryDataCenter.insertQueue(expectedQueue);
        // 2. 查询这个队列, 并比较
        MSGQueue actualQueue = memoryDataCenter.getQueue("testQueue");
        Assertions.assertEquals(expectedQueue, actualQueue);
        // 3. 删除这个队列
        memoryDataCenter.deleteQueue("testQueue");
        // 4. 再次查询队列, 看是否能查到
        actualQueue = memoryDataCenter.getQueue("testQueue");
        Assertions.assertNull(actualQueue);
    }

    @Test
    public void testBinding() throws MqException {
        Binding expectedBinding = new Binding();
        expectedBinding.setExchangeName("testExchange");
        expectedBinding.setQueueName("testQueue");
        expectedBinding.setBindingKey("testBindingKey");
        memoryDataCenter.insertBinding(expectedBinding);
    }

}