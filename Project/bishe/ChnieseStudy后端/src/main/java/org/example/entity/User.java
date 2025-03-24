package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 自增主键id
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * 用户昵称
     */
    @TableField(value = "nick_name")
    private String nickName;

    /**
     * 用户头像路径
     */
    @TableField(value = "user_pic")
    private String userPic;

    /**
     * 登录名称
     */
    @TableField(value = "login_name")
    private String loginName;

    /**
     * 用户密码
     */
    @TableField(value = "password")
    private String password;

    @TableField(value = "score")
    private int score;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public void setScore(int score) {
        this.score = score;
    }
}