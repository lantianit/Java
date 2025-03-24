package org.example.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户观看历史记录表
 * </p>
 *
 * @author author
 * @since 2025-02-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_watch_history")
public class UserWatchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 历史记录ID
     */
    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 视频ID
     */
    @TableField("video_id")
    private Long videoId;

    /**
     * 开始观看时间
     */
    @TableField("start_time")
    private Date startTime;

    /**
     * 观看进度（秒）
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 是否看完（0-未看完 1-已看完）
     */
    @TableField("is_finished")
    private Boolean isFinished;


}
