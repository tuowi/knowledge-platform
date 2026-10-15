package com.itheima.knowledge_platform.exception;

/**
 * 用户错误
 */
public class UserErrorException extends BaseException {
    public UserErrorException() {
    }

    public UserErrorException(String msg) {
        super(msg);
    }
}
