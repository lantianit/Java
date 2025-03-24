package org.example.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.TVideoComment;
import org.example.entity.VO.TVideoCommentVO;

import java.util.List;

/**
* @author 12109
* @description 针对表【t_video_comment(视频评论表)】的数据库操作Service
* @createDate 2024-12-04 22:10:05
*/
public interface TVideoCommentService extends IService<TVideoComment> {

    List<TVideoCommentVO> getList(int videoId);
    boolean saveComment(TVideoComment comment);
}
