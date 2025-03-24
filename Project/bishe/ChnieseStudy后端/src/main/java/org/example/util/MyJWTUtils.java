package org.example.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class MyJWTUtils {

    /**
     * 根据 JWT 中的 Token 获取用户 uid
     *
     * @param token
     * @return
     */
    public static Long getUidByToken(String token) {
        if (StringUtils.hasLength(token)) {
            JWT jwt = JWTUtil.parseToken(token);
            return Long.parseLong(String.valueOf(jwt.getPayload("userId")));
        }
        return 0L;
    }

}
