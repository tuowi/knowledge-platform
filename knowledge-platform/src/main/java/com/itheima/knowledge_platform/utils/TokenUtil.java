package com.itheima.knowledge_platform.utils;

public class TokenUtil {

    /**
     * 从 Authorization 请求头中提取真实Token
     */
    public static String getRealToken(String authorization) {
        if (!org.springframework.util.StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
