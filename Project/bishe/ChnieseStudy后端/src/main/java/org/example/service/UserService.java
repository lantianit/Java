package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.User;

import java.util.List;

public interface UserService  extends IService<User> {
    // 用户登录验证方法
    boolean login(String loginName, String password);

    // 用户注册方法
    boolean register(User user);

    User getUserByLoginName(User user);

    List<User> getUserScoreRankList();

    void addPoints(int userId, int points);
}