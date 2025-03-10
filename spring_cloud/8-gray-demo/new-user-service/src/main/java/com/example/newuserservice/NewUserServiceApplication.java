package com.example.newuserservice;

import com.loadbalancer.gray.GrayLoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@LoadBalancerClients(defaultConfiguration =
        GrayLoadBalancerConfig.class)
public class NewUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewUserServiceApplication.class, args);
    }

}
