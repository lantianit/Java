package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 视频表
 * @TableName video
 */
@TableName(value ="video")
@Data
public class Video implements Serializable {
    /**
     * 视频id
     */
    @TableId(value = "video_id", type = IdType.AUTO)
    private Long videoId;

    /**
     * 视频链接
     */
    @TableField(value = "video_url")
    private String videoUrl;

    /**
     * 视频标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 视频时长
     */
    @TableField(value = "duration")
    private String duration;

    /**
     * 视频简介
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 
     */
    @TableField(value = "is_recommend")
    private Integer isRecommend;

    /**
     * 视频所属分类
     */
    @TableField(value = "type")
    private String type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}