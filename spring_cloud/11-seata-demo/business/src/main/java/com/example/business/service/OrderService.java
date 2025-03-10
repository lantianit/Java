package com.example.business.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@FeignClient(name = "seata-server-order")
public interface OrderService {
    @RequestMapping("/order/create")
    int create(@RequestParam("userId") String userId,
               @RequestParam("commodityCode") String commodityCode,
               @RequestParam("orderCount") int orderCount);
}
