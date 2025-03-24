package org.example.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.TVideoComment;
import org.example.entity.VO.TVideoCommentVO;
import org.example.mapper.TVideoCommentMapper;
import org.example.service.TVideoCommentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 12109
* @description 针对表【t_video_comment(视频评论表)】的数据库操作Service实现
* @createDate 2024-12-04 22:10:05
*/
@Service
public class TVideoCommentServiceImpl extends ServiceImpl<TVideoCommentMapper, TVideoComment>
    implements TVideoCommentService {

    @Resource
    private TVideoCommentMapper commentMapper;


    @Override
    public List<TVideoCommentVO> getList(int videoId) {
        return commentMapper.getList(videoId);
    }

    @Override
    public boolean saveComment(TVideoComment comment) {
        return this.save(comment);
    }
}




