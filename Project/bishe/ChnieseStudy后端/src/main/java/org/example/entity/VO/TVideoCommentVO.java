package org.example.entity.VO;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import org.example.entity.TVideoComment;

import java.io.Serializable;

@Setter
@Getter
public class TVideoCommentVO extends TVideoComment implements Serializable {
    @TableField(value = "nick_name")
    private String nickName;
    @TableField(value = "user_pic")
    private String userPic;
}
