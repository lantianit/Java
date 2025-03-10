package com.example.consumer.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer;
import com.alibaba.nacos.client.env.NacosClientProperties;
import jakarta.annotation.Resource;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

//@LoadBalancerClients(defaultConfiguration = NacosLoadBalancerConfig.class)
//public class NacosLoadBalancerConfig {
//    @Resource
//    private NacosDiscoveryProperties nacosDiscoveryProperties;
//    @Bean
//    public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(
//            Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
//        String name = environment.getProperty("loadbalancer.client.name");
//        return new NacosLoadBalancer(
//                loadBalancerClientFactory.getLazyProvider(name,
//                        ServiceInstanceListSupplier.class), name
//                ,nacosDiscoveryProperties);
//    }
//}

public class NacosLoadBalancerConfig {
}