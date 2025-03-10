package com.example.newlogservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class LogController {
    @RequestMapping("/getlog")
    public String getLog(){
        return "测试版：getLog";
    }
}
