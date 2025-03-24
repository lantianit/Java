package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("character_video_mapping")
public class CharacterVideoMapping {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("`character`")
    private String character;

    private Long videoId;
    private Integer startTime;
} 
