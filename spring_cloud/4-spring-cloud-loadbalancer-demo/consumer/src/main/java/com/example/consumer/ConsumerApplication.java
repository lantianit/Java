package com.example.consumer;

import com.example.consumer.config.CustomLoadBalancerConfig;
import com.example.consumer.config.NacosLoadBalancerConfig;
import com.example.consumer.config.RandomLoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients // 开启 Openfeign
// 设置全局的负载均衡策略
@LoadBalancerClients(defaultConfiguration =
        CustomLoadBalancerConfig.class)
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

}
