package org.example.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.UserWatchHistory;
import org.example.entity.VO.VideoWatchVO;

import java.util.List;

/**
 * <p>
 * 用户观看历史记录表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-02-12
 */
public interface UserWatchHistoryMapper extends BaseMapper<UserWatchHistory> {

    @Select("SELECT v.video_id, v.video_url, v.title, v.duration, v.description, v.create_time, v.update_time, v.is_recommend, v.type, " +
            "uwh.user_id, uwh.start_time " +
            "FROM video v " +
            "JOIN user_watch_history uwh ON v.video_id = uwh.video_id " +
            "WHERE uwh.user_id = #{userId}")
    List<VideoWatchVO> getWatchHistoryByUserId(@Param("userId") int userId);
}
