package com.example.orderservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/order")
public class OrderController {

    @RequestMapping("/getcount")
    public int getCount(){
        return new Random().nextInt(1000);
    }

}
