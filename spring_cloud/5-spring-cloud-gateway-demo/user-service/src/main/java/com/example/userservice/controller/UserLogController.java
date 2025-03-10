package com.example.userservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RequestMapping("/userlog")
@RestController
public class UserLogController {
    @RequestMapping("/getlog")
    public String getLog() {
        return "UserService：log=javacn-" +
                new Random().nextInt(1000);
    }
}
