package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.UserVideoFavorite;
import org.example.entity.VO.FavoritedVideoVO;

import java.util.List;

public interface UserVideoFavoriteService extends IService<UserVideoFavorite> {
    boolean isVideoFavoritedByUser(int userId, long videoId);
    List<FavoritedVideoVO> getFavoritedVideosByUserId(int userId);
} 