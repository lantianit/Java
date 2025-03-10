package com.example.consumer.controller;

import com.example.consumer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BusinessController {

    @Autowired
    private UserService userService;

    @RequestMapping("/getnamebyid")
    public String getNameById(Integer id){
        return userService.getNameById(id);
    }

}
