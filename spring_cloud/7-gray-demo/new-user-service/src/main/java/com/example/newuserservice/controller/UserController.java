package com.example.newuserservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/getname")
    public String getName(){
        return "测试版：User Service getName.";
    }
}
