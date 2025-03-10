package com.example.business.controller;

import com.example.business.service.BusinessService;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class BusinessController {

    @Resource
    private BusinessService businessService;


    @RequestMapping("/purchase")
    public String purchase(@RequestParam String userId,
                           @RequestParam String commodityCode,
                           @RequestParam Integer orderCount) {
        businessService.purchase(userId, commodityCode, orderCount);
        return "ok";
    }

    @RequestMapping("/buy")
    public String buy(@RequestParam String userId,
                            @RequestParam String commodityCode,
                            @RequestParam Integer orderCount) {
        businessService.buy(userId, commodityCode, orderCount);
        return "ok";
    }
}
