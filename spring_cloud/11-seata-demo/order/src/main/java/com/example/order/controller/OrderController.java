package com.example.order.controller;

import com.example.order.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @RequestMapping("/create")
    public int create(@RequestParam("userId") String userId,
                      @RequestParam("commodityCode") String commodityCode,
                      @RequestParam("orderCount") Integer orderCount) {
        return orderService.create(userId, commodityCode, orderCount);
    }

}
