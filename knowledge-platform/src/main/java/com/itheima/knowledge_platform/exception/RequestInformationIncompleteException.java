package com.itheima.knowledge_platform.exception;

/**
 * 请求体数据不全面
 */
public class RequestInformationIncompleteException extends BaseException {
    public RequestInformationIncompleteException() {
    }

    public RequestInformationIncompleteException(String msg) {
        super(msg);
    }
}
