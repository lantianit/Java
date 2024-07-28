package com.example.java_gobang.study;


import com.example.java_gobang.config.WebSocketConfig;
import com.example.java_gobang.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class Study {
    private ConcurrentHashMap<Integer, WebSocketSession> gameHall = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, WebSocketSession> gameRoom = new ConcurrentHashMap<>();
    
    public void enterGameHall(int userId, WebSocketSession webSocketSession) {
        gameHall.put(userId, webSocketSession);
    }

    public void exitGameHall(int userId) {
        gameHall.remove(userId);
    }
    
}