package com.itheima.knowledge_platform.exception;

/**
 * 管理员错误
 */
public class PlatformAdminErrorException extends BaseException {
    public PlatformAdminErrorException() {
    }

    public PlatformAdminErrorException(String msg) {
        super(msg);
    }
}
