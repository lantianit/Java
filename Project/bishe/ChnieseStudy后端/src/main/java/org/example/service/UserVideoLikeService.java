package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.UserVideoLike;
import org.example.entity.VO.LikedVideoVO;

import java.util.List;

public interface UserVideoLikeService extends IService<UserVideoLike> {
    boolean isVideoLikedByUser(int userId, long videoId);
    List<LikedVideoVO> getLikedVideosByUserId(int userId);
} 