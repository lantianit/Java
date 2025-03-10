package com.example.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RequestMapping("/v2/user")
@RestController
public class UserControllerV2 {
    @Autowired
    private ServletWebServerApplicationContext context;

    @RequestMapping("/getname")
    public String getName() {
        return context.getWebServer().getPort() +
                "——V2：UserService：name=javacn-" +
                new Random().nextInt(10);
    }
}
