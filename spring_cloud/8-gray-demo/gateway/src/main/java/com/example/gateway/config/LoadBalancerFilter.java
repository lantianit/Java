package com.example.gateway.config;


import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.loadbalancer.gray.*;

@Component
public class LoadBalancerFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 得到 request、response 对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 判断灰度标签
        if (request.getQueryParams().getFirst(GlobalVariable.GRAY_TAGE) != null) {
            // 设置灰度标识
            response.getHeaders().set(GlobalVariable.GRAY_TAGE,
                    "true");
        }
        // 此步骤正常，执行下一步
        return chain.filter(exchange);
    }
}