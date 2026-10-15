package com.itheima.knowledge_platform.exception;

/**
 * 查询条件有问题
 */
public class QueryErrorException extends BaseException {
    public QueryErrorException() {
    }

    public QueryErrorException(String msg) {
        super(msg);
    }
}
