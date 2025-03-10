package com.example.consumer.controller;

import com.example.consumer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CallController {

    @Autowired
    private UserService userService;

    @RequestMapping("/getname")
    public String getName(@RequestParam("id") Integer id) {
        return userService.getName(id);
    }

}
