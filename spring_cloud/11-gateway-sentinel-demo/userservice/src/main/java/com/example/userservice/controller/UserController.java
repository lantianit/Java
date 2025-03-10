package com.example.userservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/user")
public class UserController {

    @RequestMapping("getid")
    public String getId(Integer id) throws InterruptedException {
        Thread.sleep(100);
        return "ID:" + id;
    }

    @RequestMapping("getname")
    public String getName() throws InterruptedException {
        Thread.sleep(100);
        return "Name:" + new Random().nextInt(100);
    }

}