package org.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.R;
import org.example.entity.TVideoComment;
import org.example.entity.User;
import org.example.entity.VO.TVideoCommentVO;
import org.example.service.TVideoCommentService;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private TVideoCommentService videoCommentService;

    @GetMapping("/getList")
    public R getList() {
        List<TVideoComment> list = videoCommentService.list();
        Map<String, Object> map = new HashMap<>();
        map.put("message",list);
        return R.ok(map);
    }

}
