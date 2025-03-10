package com.example.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.order.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper extends BaseMapper<Order> {

    @Insert("insert into `order`(user_id, commodity_code, count) values " +
            "(#{userId}, #{commodityCode}, #{orderCount})")
    int create(@Param("userId") String userId,
               @Param("commodityCode") String commodityCode,
               @Param("orderCount") int orderCount);
}
