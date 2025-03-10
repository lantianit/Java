package com.example.mq.mqserver.datacenter;

import com.example.mq.common.MqException;
import com.example.mq.mqserver.core.Binding;
import com.example.mq.mqserver.core.Exchange;
import com.example.mq.mqserver.core.MSGQueue;
import com.example.mq.mqserver.core.Message;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DiskDataCenter {

    private DataBaseManager dataBaseManager = new DataBaseManager();
    private MessageFileManager messageFileManager = new MessageFileManager();

    public void init() {
        dataBaseManager.init();
        messageFileManager.init();
    }

    // 封装交换机操作
    public void insertExchange(Exchange exchange) {
        dataBaseManager.insertExchange(exchange);
    }

    public void deleteExchange(String exchangeName) {
        dataBaseManager.deleteExchange(exchangeName);
    }

    public List<Exchange> selectAllExchanges() {
        return dataBaseManager.selectAllExchanges();
    }

    public void insertQueue(MSGQueue queue) throws IOException {
        dataBaseManager.insertQueue(queue);
        messageFileManager.createQueueFiles(queue.getName());
    }

    public void deleteQueue(String queueName) throws IOException {
        dataBaseManager.deleteQueue(queueName);
        messageFileManager.destroyQueueFiles(queueName);
    }

    public List<MSGQueue> selectAllQueues() {
        return dataBaseManager.selectAllQueues();
    }

    // 封装绑定操作
    public void insertBinding(Binding binding) {
        dataBaseManager.insertBinding(binding);
    }

    public void deleteBinding(Binding binding) {
        dataBaseManager.deleteBinding(binding);
    }

    public List<Binding> selectAllBindings() {
        return dataBaseManager.selectAllBindings();
    }

    public void sendMessage(MSGQueue queue, Message message) throws IOException, MqException {
        messageFileManager.sendMessage(queue, message);
    }

    public void deleteMessage(MSGQueue queue, Message message) throws IOException, ClassNotFoundException, MqException {
        messageFileManager.deleteMessage(queue, message);
        if (messageFileManager.checkGC(queue.getName())) {
            messageFileManager.gc(queue);
        }
    }

    public LinkedList<Message> loadAllMessageFromQueue(String queueName) throws IOException, MqException, ClassNotFoundException {
        return messageFileManager.loadAllMessageFromQueue(queueName);
    }

}