package com.itheima.knowledge_platform.exception;

/**
 * 该用户未担任队伍队长
 */
public class UserIsNotCaptainException extends BaseException {
    public UserIsNotCaptainException() {
    }

    public UserIsNotCaptainException(String msg) {
        super(msg);
    }
}
