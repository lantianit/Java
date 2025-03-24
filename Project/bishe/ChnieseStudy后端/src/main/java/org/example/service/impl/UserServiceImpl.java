package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.User;
import org.example.service.UserService;
import org.example.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean login(String loginName, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name", loginName).eq("password", password);
        return userMapper.selectOne(queryWrapper)!= null;
    }

    @Override
    public List<User> getUserScoreRankList() {
        // 使用MybatisPlus的排序功能，按照分数（score）字段降序排列来获取用户列表，以形成排行榜
        return userMapper.selectList(null).stream()
                .sorted((u1, u2) -> u2.getScore() - u1.getScore())
                .collect(Collectors.toList());
    }

    @Override
    public boolean register(User user) {
        return userMapper.insert(user) > 0;
    }

    @Override
    public User getUserByLoginName(User user) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("login_name", user.getLoginName()));
    }

    @Override
    @Transactional
    public void addPoints(int userId, int points) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setScore(user.getScore() + points);
            userMapper.updateById(user);
        }
    }
}