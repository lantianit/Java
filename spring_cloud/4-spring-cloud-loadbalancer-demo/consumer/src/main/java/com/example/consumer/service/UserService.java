package com.example.consumer.service;

import com.example.consumer.config.CustomLoadBalancerConfig;
import com.example.consumer.config.NacosLoadBalancerConfig;
import com.example.consumer.config.RandomLoadBalancerConfig;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@FeignClient("loadbalancer-service")
// 设置局部负载均衡策略
@LoadBalancerClient(name = "loadbalancer-service",
        configuration = RandomLoadBalancerConfig.class)
public interface UserService {
    @RequestMapping("/user/getname")
    String getName(@RequestParam("id") Integer id);
}
