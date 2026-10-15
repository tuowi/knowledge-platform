package com.itheima.knowledge_platform.exception;

/**
 * 团队错误
 */
public class TeamErrorException extends BaseException{
    public TeamErrorException() {
    }

    public TeamErrorException(String msg) {
        super(msg);
    }
}
