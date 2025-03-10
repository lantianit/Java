package com.example.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.order.entity.Order;
import com.example.order.mapper.OrderMapper;
import com.example.order.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lei
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public int create(String userId, String commodityCode, int orderCount) {
        return orderMapper.create(userId, commodityCode, orderCount);
    }
}
