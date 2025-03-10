package com.example.business.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@FeignClient(name = "seata-server-storage")
public interface StorageService {
    @RequestMapping("/storage/deduct")
    void deduct(@RequestParam("commodityCode") String commodityCode,
                @RequestParam("count")  Integer count);
}
