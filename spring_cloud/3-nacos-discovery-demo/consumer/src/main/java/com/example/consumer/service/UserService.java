package com.example.consumer.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//@Service
//@FeignClient("nacos-discovery-demo") // 表示调用 nacos 中的 nacos-discovery-demo 服务
//public interface UserService {
//
//    @RequestMapping("/user/getnamebyid") // 调用生产者的"/user/getnamebyid"接口
//    public String getNameById(@RequestParam("id") int id);
//
//}

@Service
@FeignClient("nacos-discovery-demo")
public interface UserService {
    
    @RequestMapping("/user/getnamebyid")
    public String getNameById(@RequestParam("id") int id);
    
}