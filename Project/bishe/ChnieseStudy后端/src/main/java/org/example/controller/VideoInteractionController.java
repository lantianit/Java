package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.R;
import org.example.entity.UserVideoFavorite;
import org.example.entity.UserVideoLike;
import org.example.entity.VO.FavoritedVideoVO;
import org.example.entity.VO.LikedVideoVO;
import org.example.entity.VO.VideoWatchVO;
import org.example.service.UserVideoFavoriteService;
import org.example.service.UserVideoLikeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/video/interaction")
public class VideoInteractionController {

    @Resource
    private UserVideoLikeService userVideoLikeService;

    @Resource
    private UserVideoFavoriteService userVideoFavoriteService;

    @RequestMapping("/like")
    public R likeVideo(@RequestParam("userId") int userId, @RequestParam("videoId") long videoId) {
        UserVideoLike like = new UserVideoLike();
        like.setUserId(userId);
        like.setVideoId(videoId);
        userVideoLikeService.save(like);
        return R.ok("Video liked successfully");
    }

    @RequestMapping("/favorite")
    public R favoriteVideo(@RequestParam("userId") int userId, @RequestParam("videoId") long videoId) {
        UserVideoFavorite favorite = new UserVideoFavorite();
        favorite.setUserId(userId);
        favorite.setVideoId(videoId);
        userVideoFavoriteService.save(favorite);
        return R.ok("Video favorited successfully");
    }

    @RequestMapping("/isLiked")
    public R isVideoLiked(@RequestParam("userId") int userId, @RequestParam("videoId") long videoId) {
        boolean isLiked = userVideoLikeService.isVideoLikedByUser(userId, videoId);
        return R.ok(Map.of("isLiked", isLiked));
    }

    @RequestMapping("/isFavorited")
    public R isVideoFavorited(@RequestParam("userId") int userId, @RequestParam("videoId") long videoId) {
        boolean isFavorited = userVideoFavoriteService.isVideoFavoritedByUser(userId, videoId);
        return R.ok(Map.of("isFavorited", isFavorited));
    }

    @RequestMapping("/unlike")
    public R unlikeVideo(@RequestParam("userId") int userId, @RequestParam("videoId") long videoId) {
        boolean removed = userVideoLikeService.remove(
            new QueryWrapper<UserVideoLike>()
                .eq("user_id", userId)
                .eq("video_id", videoId)
        );
        return removed ? R.ok("Video unliked successfully") : R.error("Failed to unlike video");
    }

    @RequestMapping("/unfavorite")
    public R unfavoriteVideo(@RequestParam("userId") int userId, @RequestParam("videoId") long videoId) {
        boolean removed = userVideoFavoriteService.remove(
            new QueryWrapper<UserVideoFavorite>()
                .eq("user_id", userId)
                .eq("video_id", videoId)
        );
        return removed ? R.ok("Video unfavorited successfully") : R.error("Failed to unfavorite video");
    }

    @GetMapping("/getLikedVideos")
    public List<LikedVideoVO> getLikedVideos(@RequestParam("userId") int userId) {
        try {
            List<LikedVideoVO> likedVideosByUserId = userVideoLikeService.getLikedVideosByUserId(userId);
            return likedVideosByUserId;
        } catch (Exception e) {
            log.error("Error retrieving liked videos", e);
            return null;
        }
    }

    @GetMapping("/getFavoritedVideos")
    public List<FavoritedVideoVO> getFavoritedVideos(@RequestParam("userId") int userId) {
        try {
            List<FavoritedVideoVO> favoritedVideos = userVideoFavoriteService.getFavoritedVideosByUserId(userId);
            return favoritedVideos;
        } catch (Exception e) {
            log.error("Error retrieving favorited videos", e);
            return null;
        }
    }

    @GetMapping("/getLikeCount")
    public R getLikeCount(@RequestParam("videoId") long videoId) {
        try {
            int likeCount = userVideoLikeService.getLikeCountByVideoId(videoId);
            return R.ok(Map.of("likeCount", likeCount));
        } catch (Exception e) {
            log.error("Error retrieving like count for videoId: " + videoId, e);
            return R.error("Failed to retrieve like count");
        }
    }
} 