package com.example.provider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ServletWebServerApplicationContext context;

    @RequestMapping("/getnamebyid")
    public String getNameById(Integer id) throws InterruptedException {
        System.out.println("-------------------- Do Provider getNameById method."
                + LocalDateTime.now());
        Thread.sleep(1500);
        return "provider-name-" + id +
                " | port:" + context.getWebServer().getPort();
    }
}

