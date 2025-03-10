package com.example.sentineldemo3.common;

import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Objects;

//@ControllerAdvice
//@ResponseBody
@RestControllerAdvice
public class SystemException {
    @ExceptionHandler(ParamFlowException.class)
    public HashMap<String, Object> paramFlowException(ParamFlowException e) {
        return new HashMap<>() {{
            put("code", HttpStatus.TOO_MANY_REQUESTS.value());
            put("msg", "请求触发了热点限流了");
        }};
    }
}
