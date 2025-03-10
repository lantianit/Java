package com.example.sentineldemo3.common;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * 定义 Sentinel 全局自定义异常类
 */
@Component
public class SentinelExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse,
                       BlockException e) throws Exception {
        int code = HttpStatus.TOO_MANY_REQUESTS.value();
        String msg = "未知异常";
        if (e instanceof FlowException) { // 被限流了
            msg = "请求被限流了";
        } else if (e instanceof DegradeException) { // 被熔断了
            msg = "请求被熔断了";
        } else if (e instanceof ParamFlowException) { // 触发热点限流
            msg = "请求触发了热点限流";
        } else if (e instanceof AuthorityException) { // 授权规则
            code = HttpStatus.UNAUTHORIZED.value();
            msg = "暂无权限";
        }
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.setStatus(code);
        httpServletResponse.getWriter().println("{\"code\":" + code + ",\"msg\":\"" +
                msg + "\"}");
    }
}
