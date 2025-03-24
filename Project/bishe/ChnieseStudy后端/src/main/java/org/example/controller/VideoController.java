package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.*;
import org.example.entity.VO.NewVideoVO;
import org.example.entity.VO.TVideoCommentVO;
import org.example.entity.VO.VideoWatchVO;
import org.example.service.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/video")
@Slf4j
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class VideoController {

    @Resource
    private VideoService videoService;

    @Resource
    private TVideoCommentService videoCommentService;

    @Resource
    private UserService userService;

    @Resource
    private UserWatchHistoryService userWatchHistoryService;

    @Resource
    private CharacterVideoMappingService characterVideoMappingService;

    @Resource
    private BigtypeService bigtypeService;

    // 记录用户观看视频的开始时间
    private Map<Integer, LocalDateTime> userVideoStartTimes = new HashMap<>();

    @GetMapping("/getVideoAndCommentByVideoId")
    public R getUserByid(int videoId) {
        List<TVideoCommentVO> comments = videoCommentService.getList(videoId);
        log.info("数据库查询：{}", comments);
        return R.ok(Map.of("message", comments));
    }

    @GetMapping("/getRecommendVideo")
    public R getRecommendVideo() {
        List<Video> recommendedVideos = videoService.list(new QueryWrapper<Video>().eq("is_recommend", 1));
        return R.ok(Map.of("message", recommendedVideos));
    }

    @GetMapping("/getVideoByVideoId")
    public R getVideoById(int videoId) {
        Video video = videoService.getById(videoId);
        return R.ok(Map.of("message", video));
    }

    @GetMapping("/getAllVideos")
    public List<Video> getAllVideos() {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("title"); // Sort by title in ascending order
        return videoService.list(queryWrapper);
    }

    @Cacheable(value = "bigTypeVideos", key = "'all'")
    @GetMapping("/getBigTypeVideo")
    public R getBigTypeVideo() {
        List<Bigtype> bigtypesList = bigtypeService.list();
        bigtypesList.forEach(bigtype -> {
            List<Video> videos = videoService.list(new QueryWrapper<Video>().eq("type", bigtype.getName()).orderByAsc("title"));
            bigtype.setBigTypeVideoList(videos);
        });
        return R.ok(Map.of("message", bigtypesList));
    }

    @PostMapping("/addComment")
    public boolean addComment(@RequestBody TVideoComment comment) {
        if (isCommentValid(comment)) {
            comment.setCreateTime(new Date());
            comment.setUpdateTime(new Date());
            log.info("comment = {}", comment);
            return videoCommentService.saveComment(comment);
        }
        return false;
    }

    @PostMapping("/updateVideo")
    public boolean updateVideo(@RequestParam("videoFile") MultipartFile videoFile,
                               @RequestParam("videoData") String videoData) {
        try {
            NewVideoVO newVideo = new ObjectMapper().readValue(videoData, NewVideoVO.class);
            if (!videoFile.isEmpty()) {
                String newFileName = saveVideoFile(videoFile);
                Video updatedVideo = createVideoFromVO(newVideo, newFileName);
                UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("video_Id", updatedVideo.getVideoId());
                videoService.update(updatedVideo, updateWrapper);
                return true;
            }
            log.info("Received video data: {}", newVideo);
            return false;
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Error updating video", e);
            return false;
        }
    }

    @PostMapping("/addVideos")
    public R addVideos(@RequestParam("videoFiles") MultipartFile[] videoFiles,
                   @RequestParam("videoData") String[] videoDataArray) {
        try {
            for (int i = 0; i < videoFiles.length; i++) {
                MultipartFile videoFile = videoFiles[i];
                NewVideoVO newVideo = new ObjectMapper().readValue(videoDataArray[i], NewVideoVO.class);
                if (!videoFile.isEmpty()) {
                    String newFileName = saveVideoFile(videoFile);
                    Video video = createVideoFromVO(newVideo, newFileName);
                    videoService.save(video);
                }
            }
            return R.ok();
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Error adding videos", e);
            return R.error("Failed to add videos");
        }
    }

    @DeleteMapping("/deleteByVideoId")
    public boolean deleteByVideoId(@RequestParam("videoId") int videoId) {
        return videoService.removeByVideoId(videoId) == 1;
    }

    static class VideoStartRequest {
        private int userId;
        private int videoId;
        // Getters and Setters
        public int getUserId() {
            return userId;
        }
        public void setUserId(int userId) {
            this.userId = userId;
        }
        public int getVideoId() {
            return videoId;
        }
        public void setVideoId(int videoId) {
            this.videoId = videoId;
        }
    }

    @RequestMapping("/startVideo")
    public R startVideo(@RequestBody VideoStartRequest request) {
        int userId = request.getUserId();
        int videoId = request.getVideoId();
        userVideoStartTimes.put(userId, LocalDateTime.now());
        log.info("User {} started watching video {}", userId, videoId);
        return R.ok();
    }

    @PostMapping("/endVideo")
    public R endVideo(@RequestBody VideoStartRequest request) {
        int userId = request.getUserId();
        int videoId = request.getVideoId();
        LocalDateTime startTime = userVideoStartTimes.get(userId);
        if (startTime != null) {
            LocalDateTime endTime = LocalDateTime.now();
            Duration duration = Duration.between(startTime, endTime);
            long minutesWatched = duration.toMinutes();
            log.info("User {} watched video {} for {} minutes", userId, videoId, minutesWatched);

            if (minutesWatched >= 10) {
                int pointsToAdd = (int) (minutesWatched / 10);
                userService.addPoints(userId, pointsToAdd);
                log.info("Added {} points to user {}", pointsToAdd, userId);
            }

            userVideoStartTimes.remove(userId);
            return R.ok();
        } else {
            return R.error("No start time recorded for user " + userId);
        }
    }

    private boolean isCommentValid(TVideoComment comment) {
        return comment.getVideoId() != null && comment.getVideoId() != 0 &&
               comment.getComment() != null && !comment.getComment().isEmpty() &&
               comment.getUserId() != null && comment.getUserId() != 0;
    }

    private String saveVideoFile(MultipartFile videoFile) throws IOException, NoSuchAlgorithmException {
        String md5 = calculateMD5(videoFile.getInputStream());
        String fileExtension = getFileExtension(videoFile.getOriginalFilename());
        String newFileName = md5 + fileExtension;
        String storageDir = "D:\\毕设视频存储\\小程序视频\\videos";
        File directory = new File(storageDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        Path filePath = Paths.get(storageDir, newFileName);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            InputStream is = videoFile.getInputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        log.info("Video file saved as: {}", newFileName);
        return newFileName;
    }

    private Video createVideoFromVO(NewVideoVO newVideo, String videoUrl) {
        Video video = new Video();
        video.setVideoId(newVideo.getVideoId());
        video.setVideoUrl(videoUrl);
        video.setTitle(newVideo.getTitle());
        video.setDuration(newVideo.getDuration());
        video.setDescription(newVideo.getDescription());
        video.setIsRecommend(newVideo.getIsRecommend());
        video.setType(newVideo.getType());
        return video;
    }

    private String calculateMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex != -1 ? fileName.substring(lastIndex) : "";
    }

    // 更新用户观看记录
    @PostMapping("/updateWatchHistory")
    public R updateWatchHistory(@RequestBody UserWatchHistory watchHistory) {
        try {
            watchHistory.setStartTime(new Date());
            // 使用 saveOrUpdate 方法，MyBatis-Plus 会根据主键或唯一索引来判断是插入还是更新
            userWatchHistoryService.saveOrUpdate(watchHistory, 
                new QueryWrapper<UserWatchHistory>()
                    .eq("user_id", watchHistory.getUserId())
                    .eq("video_id", watchHistory.getVideoId())
            );
            log.info("Updated watch history for user {} and video {}", watchHistory.getUserId(), watchHistory.getVideoId());
            return R.ok();
        } catch (Exception e) {
            log.error("Error updating watch history", e);
            return R.error("Failed to update watch history");
        }
    }

    // 获取用户历史观看记录，返回List<VideoWatchVO>
    @RequestMapping("/getWatchHistory")
    public List<VideoWatchVO> getWatchHistory(@RequestParam("userId") int userId) {
        try {
            List<VideoWatchVO> watchHistory = userWatchHistoryService.getWatchHistoryByUserId(userId);
            return watchHistory;
        } catch (Exception e) {
            log.error("Error retrieving watch history", e);
            return null;
        }
    }

    @GetMapping("/searchByTitle")
    public List<Video> searchVideosByTitle(@RequestParam("title") String title) {
        // 去除多余的引号
        if (title.startsWith("\"") && title.endsWith("\"")) {
            title = title.substring(1, title.length() - 1);
        }
        return videoService.findVideosByTitle(title);
    }

    @GetMapping("/getVideoForCharacter")
    public R getVideoForCharacter(@RequestParam("character") String character) {
        CharacterVideoMapping mapping = characterVideoMappingService.getMappingByCharacter(character);
        if (mapping != null) {
            Video video = videoService.getById(mapping.getVideoId());
            if (video != null) {
                return R.ok(Map.of(
                    "videoUrl", video.getVideoUrl(),
                    "startTime", mapping.getStartTime()
                ));
            }
        }
        return R.error("No video found for character");
    }

    @PostMapping("/associateCharacter")
    public R associateCharacter(@RequestBody Map<String, Object> request) {
        String character = (String) request.get("character");
        Long videoId = Long.valueOf((String) request.get("videoId"));
        Integer startTime = Integer.valueOf((String) request.get("startTime"));

        CharacterVideoMapping mapping = new CharacterVideoMapping();
        mapping.setCharacter(character);
        mapping.setVideoId(videoId);
        mapping.setStartTime(startTime);

        boolean success = characterVideoMappingService.save(mapping);
        return success ? R.ok() : R.error("Failed to associate character with video");
    }

    @GetMapping("/searchCharacter")
    public List<CharacterVideoMapping> searchCharacter(@RequestParam("character") String character) {
        return characterVideoMappingService.findByCharacterLike(character);
    }

    @PostMapping("/addVideo")
    public R addVideo(@RequestParam("videoFile") MultipartFile videoFile,
                      @RequestParam("videoData") String videoData) {
        try {
            NewVideoVO newVideo = new ObjectMapper().readValue(videoData, NewVideoVO.class);
            if (!videoFile.isEmpty()) {
                String newFileName = saveVideoFile(videoFile);
                Video video = createVideoFromVO(newVideo, newFileName);
                videoService.save(video);
                return R.ok();
            }
            return R.error("Video file is empty");
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Error adding video", e);
            return R.error("Failed to add video");
        }
    }
}


