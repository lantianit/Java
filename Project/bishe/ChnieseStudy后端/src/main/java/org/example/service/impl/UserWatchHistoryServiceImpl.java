package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.UserWatchHistory;
import org.example.entity.VO.VideoWatchVO;
import org.example.mapper.UserWatchHistoryMapper;
import org.example.service.UserWatchHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 用户观看历史记录表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-02-12
 */
@Service
public class UserWatchHistoryServiceImpl extends ServiceImpl<UserWatchHistoryMapper, UserWatchHistory> implements UserWatchHistoryService {

    @Resource
    private UserWatchHistoryMapper userWatchHistoryMapper;

    @Override
    public List<UserWatchHistory> getHistoryByUserId(int userId) {
        return userWatchHistoryMapper.selectList(
                new QueryWrapper<UserWatchHistory>()
                        .eq("user_id", userId)
                        // 按start_time降序排列（最近观看的在前）
                        .orderByDesc("start_time")
        );
    }

    @Override
    public List<VideoWatchVO> getWatchHistoryByUserId(int userId) {
        return userWatchHistoryMapper.getWatchHistoryByUserId(userId);
    }
}
