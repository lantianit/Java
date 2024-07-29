package com.example.java_gobang.study;

import com.example.java_gobang.game.GameReadyResponse;
import com.example.java_gobang.game.OnlineUserManager;
import com.example.java_gobang.game.Room;
import com.example.java_gobang.game.RoomManager;
import com.example.java_gobang.model.User;
import com.example.java_gobang.model.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.websocket.WsSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;

@Component
class StudyGameAPI extends TextWebSocketHandler {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private RoomManager roomManager;
    
    @Autowired
    private OnlineUserManager onlineUserManager;
    
    @Resource
    private UserMapper userMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        GameReadyResponse resp = new GameReadyResponse();
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            resp.setOk(false);
            resp.setReason("用户未登录");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if (room == null) {
            resp.setOk(false);
            resp.setReason("用户尚未匹配成功");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }
        // 2. 判定当前用户是否已经进入房间. (拿着房间管理器进行查询)
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if (room == null) {
            // 如果为 null, 当前没有找到对应的房间. 该玩家还没有匹配到.
            resp.setOk(false);
            resp.setReason("用户尚未匹配到!");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        // 3. 判定当前是不是多开 (该用户是不是已经在其他地方进入游戏了)
        //    前面准备了一个 OnlineUserManager
        if (onlineUserManager.getFromGameHall(user.getUserId()) != null
                || onlineUserManager.getFromGameRoom(user.getUserId()) != null) {
            // 如果一个账号, 一边是在游戏大厅, 一边是在游戏房间, 也视为多开~~
            resp.setOk(true);
            resp.setReason("禁止多开游戏页面");
            resp.setMessage("repeatConnection");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        onlineUserManager.enterGameRoom(user.getUserId(), session);
        
        synchronized (room) {
            if (room.getUser1() == null) {
                room.setUser1(user);
                room.setWhiteUser(user.getUserId());
                return;
            }
            if (room.getUser2() == null) {
                room.setUser2(user);
                not
            }
        }
        
    }
    
}














