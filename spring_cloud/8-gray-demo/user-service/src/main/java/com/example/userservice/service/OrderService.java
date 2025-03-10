package com.example.userservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("order-service-gray")
@Service
public interface OrderService {
    @RequestMapping("/order/getorder")
    public String getOrder();
}
