package com.example.sentineldemo3.controller;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/user")
public class UserController {

    @SentinelResource(value = "/user/getid",entryType = EntryType.IN)
    @RequestMapping("getid")
    public String getId(Integer id) throws InterruptedException {
        Thread.sleep(100);
        return "ID:" + id;
    }

    @RequestMapping("getname")
    public String getName() throws InterruptedException {
        Thread.sleep(100);
        return "Name:" + new Random().nextInt(100);
    }

}
