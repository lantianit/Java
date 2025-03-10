package com.example.sentineldemo2;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SentinelDemo2Application {

    public static void main(String[] args) {
        SpringApplication.run(SentinelDemo2Application.class, args);
        initFlowRules(); // 启动的时候加载限流规则
        initDegradeRules(); // 启动的时候加载熔断规则
    }

    /**
     * 设置熔断规则
     */
    private static void initDegradeRules(){
        List<DegradeRule> degradeRules = new ArrayList<>();
        // 设置一条熔断规则
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource("getname"); // 熔断的资源名
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT); // 熔断类型：[慢调用]、异常比例、异常个数
        degradeRule.setCount(10); // 熔断类型的值，如果是慢调用就是慢调用的值，单位是毫秒
        degradeRule.setStatIntervalMs(1000); // 熔断的统计时间，单位是毫秒
        degradeRule.setSlowRatioThreshold(0.5); // 慢调用比例，此值是 0~1 之间的小数
        degradeRule.setMinRequestAmount(1); // 熔断的最小请求数
        degradeRule.setTimeWindow(5); // 熔断的时长，时间单位是毫秒
        degradeRules.add(degradeRule);
        DegradeRuleManager.loadRules(degradeRules);
    }


    /**
     * 定义限流规则
     */
    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();

        // 定义一个限流规则
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("getname"); // 资源名|必须参数
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS); // 限流指标：QPS/线程数|必须参数
        flowRule.setCount(1); // 限流数量（上一步 QPS 或线程数的值）|必须参数
        rules.add(flowRule);

        FlowRuleManager.loadRules(rules);
    }

}
