package org.example.entity.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;


@Data
public class VideoWatchVO implements Serializable {
    /**
     * 视频id
     */
    private Long videoId;

    /**
     * 视频链接
     */
    private String videoUrl;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频时长
     */
    private String duration;

    /**
     * 视频简介
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     *
     */
    private Integer isRecommend;

    /**
     * 视频所属分类
     */
    private String type;

    private Integer userId;

    private Date startTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}