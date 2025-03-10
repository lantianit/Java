package com.example.business.service;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BusinessService {
    @Resource
    private StorageService storageService;
    @Resource
    private OrderService orderService;

    @Transactional
    public void purchase(String userId,
                         String commodityCode,
                         Integer orderCount) {

        // 添加订单
        orderService.create(userId, commodityCode, orderCount);

        // 减库存
        storageService.deduct(commodityCode, orderCount);

        // 更新账户（代码省略...）
    }


    /**
     * 不带全局事务
     */
    public void buy(String userId, String commodityCode, Integer orderCount) {
        // 添加订单
        orderService.create(userId, commodityCode, orderCount);
        // 减库存
        storageService.deduct(commodityCode, orderCount);
        // 更新账户（代码省略...）
    }
}
