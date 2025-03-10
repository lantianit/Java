package com.example.orderservice.controller;

import com.example.orderservice.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private LogService logService;

    @RequestMapping("/getorder")
    public String getOrder(){
        String result = logService.getLog();
        return "Do OrderService getOrder Method."+
                result;
    }
}
