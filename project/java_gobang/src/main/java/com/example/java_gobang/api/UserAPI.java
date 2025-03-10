package com.example.java_gobang.api;

import com.example.java_gobang.model.User;
import com.example.java_gobang.model.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class UserAPI {
    @Resource
    private UserMapper userMapper;

    @PostMapping("/login")
    public Object login(String username, String password, HttpServletRequest req) {
        User user = userMapper.selectByName(username);
        System.out.println("[login] username = " + username);
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("登录失败");
            return new User();
        }
//        HttpSession httpSession = req.getSession(true);
//        httpSession.setAttribute("user",user);if
        HttpSession httpSession = req.getSession(true);
        httpSession.setAttribute("user",user);
        return user;
    }
    
    @PostMapping("/register")
    public Object register(String username, String password) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            userMapper.insert(user);
            return user;
        } catch (org.springframework.dao.DuplicateKeyException e) {
            User user = new User();
            return user;
        }
    }
    
    @GetMapping("/userInfo")
    public Object getUserInfo(HttpServletRequest req) {
        try {
            HttpSession httpSession = req.getSession(false);
            User user = (User) httpSession.getAttribute("user");
            User newUser = userMapper.selectByName(user.getUsername());
            return newUser;
        } catch (NullPointerException e) {
            return new User();
        }
    }
}