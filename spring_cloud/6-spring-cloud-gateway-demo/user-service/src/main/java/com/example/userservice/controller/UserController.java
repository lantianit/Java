package com.example.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.Random;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ServletWebServerApplicationContext context;

    @RequestMapping("/getname")
    public String getName() {
        return context.getWebServer().getPort() +
                "——UserService：name=javacn-" +
                new Random().nextInt(100);
    }

    @RequestMapping("/print-header")
    public void printHeader(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String key = headers.nextElement();
            String value = request.getHeader(key);
            System.out.println(key + "：" + value);
        }
    }

}
