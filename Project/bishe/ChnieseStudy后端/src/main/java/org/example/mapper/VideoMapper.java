package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.entity.Video;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 12109
* @description 针对表【video(视频表)】的数据库操作Mapper
* @createDate 2024-11-20 21:24:07
* @Entity generator.domain.Video
*/
public interface VideoMapper extends BaseMapper<Video> {

    @Select("SELECT * FROM video WHERE video_id = #{videoId}")
    Video getVideoById(Long videoId);

    @Select("SELECT * FROM video WHERE title LIKE CONCAT('%', #{title}, '%')")
    List<Video> findVideosByTitle(String title);
}




