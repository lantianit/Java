package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user_video_favorite")
public class UserVideoFavorite implements Serializable {
    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Long favoriteId;

    @TableField("user_id")
    private Integer userId;

    @TableField("video_id")
    private Long videoId;

    @TableField("create_time")
    private Date createTime;
} 