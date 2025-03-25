package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.UserVideoLike;
import org.example.entity.VO.LikedVideoVO;
import org.example.entity.Video;
import org.example.mapper.UserVideoLikeMapper;
import org.example.mapper.VideoMapper;
import org.example.service.UserVideoLikeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserVideoLikeServiceImpl extends ServiceImpl<UserVideoLikeMapper, UserVideoLike> implements UserVideoLikeService {

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserVideoLikeMapper userVideoLikeMapper;

    public UserVideoLikeServiceImpl(VideoMapper videoMapper) {
        this.videoMapper = videoMapper;
    }

    @Override
    public boolean isVideoLikedByUser(int userId, long videoId) {
        QueryWrapper<UserVideoLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("video_id", videoId);
        int count = this.count(queryWrapper);
        return count > 0;
    }

    @Override
    public List<LikedVideoVO> getLikedVideosByUserId(int userId) {
        // 创建查询条件
        QueryWrapper<UserVideoLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("create_time");

        // 获取用户点赞的视频ID列表
        List<UserVideoLike> userVideoLikes = userVideoLikeMapper.selectList(queryWrapper);

        // 映射到 LikedVideoVO
        List<LikedVideoVO> likedVideos = new ArrayList<>();
        for (UserVideoLike userVideoLike : userVideoLikes) {
            // 使用 VideoMapper 获取视频信息
            Video video = videoMapper.getVideoById(userVideoLike.getVideoId());
            if (video != null) {
                LikedVideoVO likedVideoVO = new LikedVideoVO();
                likedVideoVO.setVideoId(video.getVideoId());
                likedVideoVO.setVideoUrl(video.getVideoUrl());
                likedVideoVO.setTitle(video.getTitle());
                likedVideoVO.setDescription(video.getDescription());
                likedVideoVO.setLikedTime(userVideoLike.getCreateTime());
                likedVideos.add(likedVideoVO);
            }
        }
        return likedVideos;
    }

    @Override
    public int getLikeCountByVideoId(long videoId) {
        QueryWrapper<UserVideoLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("video_id", videoId);
        return this.count(queryWrapper);
    }
} 