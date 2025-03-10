package com.example.sentineldemo3.common;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CustomOriginRequest implements RequestOriginParser {

    @Override
    public String parseOrigin(HttpServletRequest request) {
        String origin = request.getHeader("origin");
        if(!StringUtils.hasLength(origin)){
            // 请求标识为空
            origin = "blank";
        }
        return origin;
    }
}
