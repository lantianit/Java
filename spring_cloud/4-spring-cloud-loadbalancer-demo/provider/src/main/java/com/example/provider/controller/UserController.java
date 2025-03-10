package com.example.provider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ServletWebServerApplicationContext context;

    @RequestMapping("/getname")
    public String getName(@RequestParam("id") Integer id) {
        return context.getWebServer().getPort() +
                ":Provider-name-" + id;
    }

}
