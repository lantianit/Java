package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class TestController {
    @Value("${myconfig}")
    private String myconfig;

    @RequestMapping("/getconfig")
    public String getMyconfig() {
        return myconfig;
    }
}