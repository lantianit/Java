package org.example.service;

import org.example.entity.UserWatchHistory;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import org.example.entity.VO.VideoWatchVO;

public interface UserWatchHistoryService extends IService<UserWatchHistory> {
    List<UserWatchHistory> getHistoryByUserId(int userId);
    List<VideoWatchVO> getWatchHistoryByUserId(int userId);
} 