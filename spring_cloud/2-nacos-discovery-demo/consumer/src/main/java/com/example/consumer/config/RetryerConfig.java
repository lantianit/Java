package com.example.consumer.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration // 将当前对象存储在 IoC 容器
//public class RetryerConfig {
//    @Bean
//    public Retryer retryer(){
//        return new Retryer.Default(1000,1000,3);
//    }
//}
