package com.itheima.knowledge_platform.utils;

import java.util.UUID;

public class SessionUtil {
    /**
     * 生成唯一sessionId
     */
    public static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}