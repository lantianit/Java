package com.example.orderservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @RequestMapping("/getorder")
    public String getOrder(){
        return "Do OrderService getOrder Method.";
    }
}
