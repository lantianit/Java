package com.example.orderservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@FeignClient("log-service-gray")
public interface LogService {
    @RequestMapping("/log/getlog")
    String getLog();
}
