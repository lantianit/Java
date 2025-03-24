package org.example.controller;

import org.example.entity.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mystudy")
public class TestController {

    @GetMapping("/test")
    public R test() {
        return R.ok();
    }

}
