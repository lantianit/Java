package com.example.sentineldemo3.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/user")
public class UserController {

    @RequestMapping("getid")
    public String getId() throws InterruptedException {
        Thread.sleep(100);
        return "ID:" + new Random().nextInt(100);
    }

    @RequestMapping("getname")
    public String getName() {
        return "Name:" + new Random().nextInt(100);
    }

}
