package com.example.userservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/getname")
    public String getName() {
        return "UserService：name=javacn-" +
                new Random().nextInt(100);
    }
}
