package com.example.newuserservice.controller;

import com.example.newuserservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private OrderService orderService;

    @RequestMapping("/getname")
    public String getName() {
        String result = orderService.getOrder();
        return "测试版：User Service getName." +
                result;
    }
}
