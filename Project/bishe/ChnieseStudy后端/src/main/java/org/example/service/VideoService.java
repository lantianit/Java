package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Video;

import java.util.List;

/**
* @author 12109
* @description 针对表【video(视频表)】的数据库操作Service
* @createDate 2024-11-20 21:24:07
*/
public interface VideoService extends IService<Video> {

    int removeByVideoId(int videoId);

    List<Video> findVideosByTitle(String title);
}
