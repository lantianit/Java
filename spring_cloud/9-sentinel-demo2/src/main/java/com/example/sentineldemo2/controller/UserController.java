package com.example.sentineldemo2.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/getid")
    public String getId() {
        try (Entry entry = SphU.entry("getid")) { // 定义资源
            // 正常执行的业务逻辑...
            return "ID：" + new Random().nextInt(100);
        } catch (BlockException blockException) {
            // 如果执行到此处，说明当前资源已经被限流或熔断
            return "被限制";
        }
    }

    @SentinelResource(value = "getname", blockHandler = "myBlockHander")
    @RequestMapping("/getname")
    public String getName() throws InterruptedException {
        Thread.sleep(100);
        return "Name：" + new Random().nextInt(100);
    }

    /**
     * 限流之后的执行方法
     */
    public String myBlockHander(BlockException blockException) {
        if (blockException instanceof FlowException) {
            // 限流异常
            return "您被限流了";
        } else if (blockException instanceof DegradeException) {
            // 熔断异常
            return "您被熔断了";
        }
        return "被限制了";
    }

}
