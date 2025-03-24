package org.example.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.TVideoComment;
import org.example.entity.VO.TVideoCommentVO;

import java.util.List;

/**
* @author 12109
* @description 针对表【t_video_comment(视频评论表)】的数据库操作Mapper
* @createDate 2024-12-04 22:10:05
* @Entity generator.domain.TVideoComment
*/
public interface TVideoCommentMapper extends BaseMapper<TVideoComment> {
    List<TVideoCommentVO> getList(@Param("vid")Integer videoId);
}




