package com.example.logservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class LogController {
    @RequestMapping("/getlog")
    public String getLog(){
        return "生产版：getLog";
    }
}
