package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName carousel_image
 */
@TableName(value ="carousel_image")
@Data
public class CarouselImage implements Serializable {
    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 轮播图名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 图片地址
     */
    @TableField(value = "imgUrl")
    private String imgurl;

    /**
     * 排序
     */
    @TableField(value = "imgOrder")
    private Integer imgorder;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}