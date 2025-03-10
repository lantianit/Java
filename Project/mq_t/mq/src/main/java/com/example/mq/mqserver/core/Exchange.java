package com.example.mq.mqserver.core;

import java.util.HashMap;
import java.util.Map;

public class Exchange {
    
    private String name;
    private ExchangeType exchangeType = ExchangeType.DIRECT;
    private boolean durable = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    private boolean autoDelete = false;
    private Map<String, Object> arguments = new HashMap<>();
}