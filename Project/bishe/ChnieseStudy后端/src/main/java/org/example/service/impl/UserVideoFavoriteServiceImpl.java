package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.UserVideoFavorite;
import org.example.entity.VO.FavoritedVideoVO;
import org.example.entity.Video;
import org.example.mapper.UserVideoFavoriteMapper;
import org.example.mapper.VideoMapper;
import org.example.service.UserVideoFavoriteService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserVideoFavoriteServiceImpl extends ServiceImpl<UserVideoFavoriteMapper, UserVideoFavorite> implements UserVideoFavoriteService {

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserVideoFavoriteMapper userVideoFavoriteMapper;

    @Override
    public boolean isVideoFavoritedByUser(int userId, long videoId) {
        // 使用 MyBatis-Plus 提供的查询方法
        QueryWrapper<UserVideoFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("video_id", videoId);

        // 查询数据库，判断是否存在记录
        int count = this.count(queryWrapper);
        return count > 0;
    }

    @Override
    public List<FavoritedVideoVO> getFavoritedVideosByUserId(int userId) {
        // 创建查询条件
        QueryWrapper<UserVideoFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("create_time");

        // 获取用户收藏的视频ID列表
        List<UserVideoFavorite> userVideoFavorites = userVideoFavoriteMapper.selectList(queryWrapper);

        // 映射到 FavoritedVideoVO
        List<FavoritedVideoVO> favoritedVideos = new ArrayList<>();
        for (UserVideoFavorite userVideoFavorite : userVideoFavorites) {
            // 使用 VideoMapper 获取视频信息
            Video video = videoMapper.getVideoById(userVideoFavorite.getVideoId());
            if (video != null) {
                FavoritedVideoVO favoritedVideoVO = new FavoritedVideoVO();
                favoritedVideoVO.setVideoId(video.getVideoId());
                favoritedVideoVO.setVideoUrl(video.getVideoUrl());
                favoritedVideoVO.setTitle(video.getTitle());
                favoritedVideoVO.setDescription(video.getDescription());
                favoritedVideoVO.setFavoritedTime(userVideoFavorite.getCreateTime());
                favoritedVideos.add(favoritedVideoVO);
            }
        }
        return favoritedVideos;
    }
} 